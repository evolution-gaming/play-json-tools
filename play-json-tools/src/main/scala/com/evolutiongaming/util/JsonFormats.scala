package com.evolutiongaming.util

import java.net.URL
import java.time.format.{DateTimeFormatter, DateTimeParseException}
import java.time.{Instant, LocalTime, ZoneOffset}

import com.evolutiongaming.nel.{Nel => NewNel}
import play.api.libs.json._

import scala.annotation.tailrec
import scala.concurrent.duration._
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}


object JsonFormats {

  implicit val UrlFormat: Format[URL] = new Format[URL] {
    override def reads(json: JsValue): JsResult[URL] = for {
      s <- json.validate[String]
      url <- Try(new URL(s)) match {
        case Success(x) => JsSuccess(x)
        case Failure(t) => JsError(t.toString)
      }
    } yield url
    override def writes(o: URL): JsValue = JsString(o.toString)
  }


  implicit val FiniteDurationFormat: Format[FiniteDuration] = new Format[FiniteDuration] {
    def reads(json: JsValue): JsResult[FiniteDuration] = {
      def readStr = for {x <- json.validate[String]} yield Duration(x).toCoarsest.asInstanceOf[FiniteDuration]
      def readNum = for {x <- json.validate[Long]} yield x.millis
      readStr orElse readNum
    }

    def writes(o: FiniteDuration): JsValue = JsString(o.toCoarsest.toString)
  }


  implicit val InstantFormat: Format[Instant] = new Format[Instant] {
    private val Format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC)
    private val IsoFormat = DateTimeFormatter.ISO_INSTANT

    def reads(json: JsValue): JsResult[Instant] = {
      def readStr = for {x <- json.validate[String]} yield {
        val temporal = Try {Format parse x} recover { case _: DateTimeParseException => IsoFormat parse x }
        Instant from temporal.get
      }
      def readNum = for {x <- json.validate[Long]} yield Instant.ofEpochMilli(x)
      readStr orElse readNum
    }

    def writes(o: Instant): JsValue = JsString(Format format o)
  }


  implicit val LocalTimeFormat: Format[LocalTime] = new Format[LocalTime] {
    private val Format = DateTimeFormatter.ofPattern("HH:mm:ss")

    def reads(json: JsValue): JsResult[LocalTime] = {
      for {
        time <- json.validate[String]
      } yield LocalTime.parse(time, Format)
    }

    def writes(o: LocalTime): JsValue = JsString(Format format o)
  }


  class ObjectFormat[T](from: String => Option[T], to: T => String = (x: T) => x.toString)(implicit tag: ClassTag[T]) extends Format[T] {
    def reads(json: JsValue): JsResult[T] = for {
      x <- json.validate[String]
      x <- from(x) map { x => JsSuccess(x) } getOrElse JsError(s"No ${tag.runtimeClass.getName} found for $x")
    } yield x

    def writes(x: T) = JsString(to(x))
  }


  class ObjectNumericFormat[T](from: BigDecimal => Option[T], to: T => BigDecimal)(implicit tag: ClassTag[T])
    extends Format[T] {

    def reads(json: JsValue): JsResult[T] = for {
      x <- json.validate[BigDecimal]
      x <- from(x) map { x => JsSuccess(x) } getOrElse JsError(s"No ${tag.runtimeClass.getName} found for $x")
    } yield x

    def writes(x: T) = JsNumber(to(x))
  }


  class MapValuesFormat[K, V](key: V => K)(implicit format: Format[V]) extends Format[Map[K, V]] {
    def reads(json: JsValue): JsResult[Map[K, V]] = {
      for {
        xs <- json.validate[Iterable[V]]
      } yield xs.map { x => (key(x), x) }.toMap
    }

    def writes(xs: Map[K, V]): JsValue = Json.toJson(xs.values)
  }


  class AnyKeyMap[K, V](toStr: K => String, fromStr: String => K)(implicit format: Format[V]) extends Format[Map[K, V]] {
    def reads(json: JsValue): JsResult[Map[K, V]] = {
      for {x <- json.validate[Map[String, V]]} yield for {(k, v) <- x} yield (fromStr(k), v)
    }
    def writes(x: Map[K, V]): JsValue = {
      Json toJson (for {(k, v) <- x} yield (toStr(k), v))
    }
  }


  class MapFormat[K, V](keyName: String)(implicit kf: Format[K], vf: Format[V], tag: ClassTag[V]) extends Format[Map[K, V]] {
    private val fieldNotFound = try {
      tag.runtimeClass.getDeclaredField(keyName)
      false
    } catch {
      case _: NoSuchFieldException => true
    }

    require(fieldNotFound, s"Cannot use key $keyName because such field exists in ${tag.runtimeClass}")

    def reads(json: JsValue): JsResult[Map[K, V]] = {

      @tailrec def loop(jsons: List[JsValue], kvs: List[(K, V)]): JsResult[List[(K, V)]] = jsons match {
        case Nil           => JsSuccess(kvs)
        case json :: jsons =>
          val result = for {
            key <- (json \ keyName).validate[K]
            value <- (json \ "value").validate[V] orElse json.validate[V]
          } yield (key, value)

          result match {
            case result: JsError      => result
            case JsSuccess(result, _) => loop(jsons, result :: kvs)
          }
      }

      for {
        array <- json.validate[JsArray]
        kvs <- loop(array.value.toList, Nil)
      } yield kvs.toMap
    }

    def writes(x: Map[K, V]) = JsArray {
      for {
        (key, value) <- x.toSeq
      } yield {
        val json = Json toJson value match {
          case json: JsObject => json
          case json           => Json.obj("value" -> json)
        }
        Json.obj(keyName -> Json.toJson(key)) ++ json
      }
    }
  }


  class FlatFormat[T](field: String, format: OFormat[T])(implicit tag: ClassTag[T]) extends OFormat[T] {
    private val fieldNotFound = try {
      tag.runtimeClass.getField(field)
      false
    } catch {
      case _: NoSuchFieldException => true
    }

    require(fieldNotFound, s"Cannot flatten field $field because it does not exist")

    def reads(json: JsValue): JsResult[T] = for {
      obj <- json.validate[JsObject]
      result <- format.reads(obj + (field -> json))
    } yield result

    def writes(o: T): JsObject = {
      val json = format.writes(o)
      val nested = (json \ field).asOpt[JsObject] getOrElse Json.obj()
      json - field ++ nested
    }
  }

  object FlatFormat {
    def apply[T](field: String, format: OFormat[T])(implicit tag: ClassTag[T]): OFormat[T] = {
      new FlatFormat[T](field, format)
    }
  }


  implicit class ReadsOps[T](val self: Reads[T]) extends AnyVal {
    def collectSubtype[TT <: T](implicit tag: ClassTag[TT]): Reads[TT] = {
      self.collect(JsonValidationError(s"${ tag.runtimeClass } expected")) { case tag(x) => x }
    }
  }


  implicit class EitherFormats[L, R](val self: Either[L, R]) extends AnyVal {
    def jsResult: JsResult[R] = self match {
      case Left(x)  => JsError(x.toString)
      case Right(x) => JsSuccess(x)
    }
  }


  implicit def eitherFormat[L, R](implicit lf: Format[L], rf: Format[R]): Format[Either[L, R]] = new Format[Either[L, R]] {
    def writes(x: Either[L, R]): JsValue = x match {
      case Left(x)  => Json.obj("left" -> lf.writes(x))
      case Right(x) => rf.writes(x)
    }

    def reads(json: JsValue): JsResult[Either[L, R]] = {
      def left = for {left <- (json \ "left").validate[L]} yield Left(left)
      def right = for {right <- json.validate[R]} yield Right(right)
      left orElse right
    }
  }


  trait TypeFormat[T] extends OFormat[T] {
    type Pf = PartialFunction[String, JsResult[T]]

    def readsPf(json: JsValue): Pf

    def reads(json: JsValue): JsResult[T] = {
      def reads(t: String) = {
        val pf = readsPf(json)
        if (pf isDefinedAt t) pf(t)
        else JsError(s"No Reads defined for $t")
      }

      for {
        typ <- (json \ "type").validate[String]
        result <- reads(typ)
      } yield result
    }

    def writes(t: String, json: JsObject = Json.obj()): JsObject = {
      Json.obj("type" -> t) ++ json
    }
  }


  implicit def newNelFormat[T](implicit format: Format[T]): Format[NewNel[T]] = new Format[NewNel[T]] {
    def reads(json: JsValue): JsResult[NewNel[T]] = for {
      list <- json.validate[List[T]]
      nel <- list match {
        case Nil          => JsError("list is empty")
        case head :: tail => JsSuccess(NewNel(head, tail))
      }
    } yield nel

    def writes(x: NewNel[T]): JsValue = Json toJson x.toList
  }
}