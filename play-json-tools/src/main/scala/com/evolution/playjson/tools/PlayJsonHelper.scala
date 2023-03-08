package com.evolution.playjson.tools

import com.evolutiongaming.nel.Nel
import play.api.libs.json._

import java.net.URL
import java.time.format.{DateTimeFormatter, DateTimeParseException}
import java.time.{Instant, LocalTime, ZoneOffset}
import scala.concurrent.duration._
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}


object PlayJsonHelper {

  implicit val UrlFormat: Format[URL] = new Format[URL] {

    def reads(json: JsValue): JsResult[URL] = for {
      s <- json.validate[String]
      url <- Try(new URL(s)) match {
        case Success(x) => JsSuccess(x)
        case Failure(t) => JsError(t.toString)
      }
    } yield url

    def writes(o: URL): JsValue = JsString(o.toString)
  }


  implicit val FiniteDurationFormat: Format[FiniteDuration] = new Format[FiniteDuration] {

    def reads(json: JsValue) = {
      def readStr = for {x <- json.validate[String]} yield Duration(x).toCoarsest.asInstanceOf[FiniteDuration]

      def readNum = for {x <- json.validate[Long]} yield x.millis

      readStr orElse readNum
    }

    def writes(o: FiniteDuration) = JsString(o.toCoarsest.toString)
  }


  implicit val InstantFormat: Format[Instant] = new Format[Instant] {

    private val Format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC)
    private val IsoFormat = DateTimeFormatter.ISO_INSTANT

    def reads(json: JsValue): JsResult[Instant] = {
      def readStr = for {x <- json.validate[String]} yield {
        val temporal = Try { Format parse x } recover { case _: DateTimeParseException => IsoFormat parse x }
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


  object ObjectFormat {

    def apply[A](
      from: String => Option[A],
      to: A => String = (x: A) => x.toString)(implicit
      tag: ClassTag[A]
    ): Format[A] = {

      new Format[A] {

        def reads(json: JsValue) = for {
          a <- json.validate[String]
          a <- from(a) map { x => JsSuccess(x) } getOrElse JsError(s"No ${ tag.runtimeClass.getName } found for $a")
        } yield a

        def writes(a: A) = JsString(to(a))
      }
    }
  }


  object ObjectNumericFormat {

    def apply[A](from: BigDecimal => Option[A], to: A => BigDecimal)(implicit tag: ClassTag[A]): Format[A] = {

      new Format[A] {

        def reads(json: JsValue) = for {
          a <- json.validate[BigDecimal]
          a <- from(a) map { x => JsSuccess(x) } getOrElse JsError(s"No ${ tag.runtimeClass.getName } found for $a")
        } yield a

        def writes(x: A) = JsNumber(to(x))
      }
    }
  }


  object MapValuesFormat {

    def apply[K, V: Format](key: V => K): Format[Map[K, V]] = {

      new Format[Map[K, V]] {

        def reads(json: JsValue) = {
          for {
            xs <- json.validate[Iterable[V]]
          } yield {
            xs.map { x => (key(x), x) }.toMap
          }
        }

        def writes(xs: Map[K, V]) = Json.toJson(xs.values)
      }
    }
  }


  object StringKeyMapFormat {

    def apply[K, V: Format](readKey: String => Option[K], writeKey: K => String): OFormat[Map[K, V]] = {
      OFormat(reads[K, V](readKey), writes[K, V](writeKey))
    }

    def reads[K, V: Reads](readKey: String => Option[K]): Reads[Map[K, V]] = {
      StringMapReads[K, V] { a =>
        readKey(a) match {
          case Some(k) => JsSuccess(k)
          case None    => JsError(s"cannot parse key from $a")
        }
      }
    }

    def writes[K, V: Writes](writeKey: K => String): OWrites[Map[K, V]] = StringMapWrites[K, V](writeKey)
  }


  object StringMapFormat {

    def apply[K, V: Format](toStr: K => String, fromStr: String => JsResult[K]): OFormat[Map[K, V]] = {
      val writes = StringMapWrites[K, V](toStr)
      val reads = StringMapReads[K, V](fromStr)
      OFormat(reads, writes)
    }
  }


  object StringMapWrites {

    def apply[K, V: Writes](toStr: K => String): OWrites[Map[K, V]] = {

      new OWrites[Map[K, V]] {

        def writes(kvs: Map[K, V]): JsObject = {
          JsObject(
            kvs.map { case (k, v) => (toStr(k), Writes.of[V].writes(v)) }
          )
        }
      }
    }
  }


  object StringMapReads {

    def apply[K, V: Reads](fromStr: String => JsResult[K]): Reads[Map[K, V]] = Reads.mapReads[K, V](fromStr)
  }


  object MapFormatUnsafe {

    def apply[K, V](toStr: K => String, fromStr: String => K)(implicit vf: Format[V]): OFormat[Map[K, V]] = {

      val format = StringKeyMapFormat[K, V](k => Try { fromStr(k) }.toOption, toStr)

      new OFormat[Map[K, V]] {

        def reads(json: JsValue): JsResult[Map[K, V]] = format.reads(json)

        def writes(o: Map[K, V]) = format.writes(o)
      }
    }
  }


  object MapFormat {

    def apply[K: Format, V: Format](keyName: String)(implicit tag: ClassTag[V]): Format[Map[K, V]] = {

      val fieldNotFound = try {
        tag.runtimeClass.getDeclaredField(keyName)
        false
      } catch {
        case _: NoSuchFieldException => true
      }

      require(fieldNotFound, s"Cannot use key $keyName because such field exists in ${ tag.runtimeClass }")

      val reads = MapReads[K, V](keyName)
      val writes = MapWrites[K, V](keyName)
      Format[Map[K, V]](reads, writes)
    }
  }


  object MapWrites {

    def apply[K: Writes, V: Writes](keyName: String): Writes[Map[K, V]] = {

      implicit val writesTuple: Writes[(K, V)] = new Writes[(K, V)] {

        def writes(kv: (K, V)) = {
          val (k, v) = kv
          val json = Json.toJson(v) match {
            case json: JsObject => json
            case json           => Json.obj(("value", json))
          }
          Json.obj((keyName, Json.toJson(k))) ++ json
        }
      }

      new Writes[Map[K, V]] {
        def writes(kvs: Map[K, V]) = {
          Json.toJson(kvs.toList)
        }
      }
    }
  }


  object MapReads {

    def apply[K: Reads, V: Reads](keyName: String): Reads[Map[K, V]] = {

      implicit val readsTuple: Reads[(K, V)] = new Reads[(K, V)] {
        def reads(json: JsValue) = {
          for {
            k <- (json \ keyName).validate[K]
            v <- (json \ "value").validate[V] orElse json.validate[V]
          } yield {
            (k, v)
          }
        }
      }

      new Reads[Map[K, V]] {
        def reads(json: JsValue) = {
          for {
            a <- json.validate[List[(K, V)]]
          } yield {
            a.toMap
          }
        }
      }
    }
  }


  object FlatFormat {

    def apply[A](field: String, format: OFormat[A])(implicit tag: ClassTag[A]): OFormat[A] = {

      val fieldExists = try {
        tag.runtimeClass.getDeclaredField(field)
        true
      } catch {
        case _: NoSuchFieldException => false
      }

      require(fieldExists, s"Cannot flatten field $field because it does not exist")

      new OFormat[A] {

        def reads(json: JsValue): JsResult[A] = for {
          obj <- json.validate[JsObject]
          result <- format.reads(obj + (field -> json))
        } yield result

        def writes(o: A): JsObject = {
          val json = format.writes(o)
          val nested = (json \ field).asOpt[JsObject] getOrElse Json.obj()
          json - field ++ nested
        }
      }
    }
  }


  implicit class ReadsOpsPlayJsonHelper[A](val self: Reads[A]) extends AnyVal {

    def narrowReads[B <: A](implicit tag: ClassTag[B]): Reads[B] = {
      Reads[B] { json =>
        self
          .reads(json)
          .flatMap {
            case tag(a) => JsSuccess(a)
            case _      => JsError(JsonValidationError(s"${ tag.runtimeClass } expected"))
          }
      }
    }
  }


  implicit class WritesOpsPlayJsonHelper[A](val self: Writes[A]) extends AnyVal {

    def narrowWrites[B <: A]: Writes[B] = self.contramap[B](identity)
  }


  implicit class OWritesOpsPlayJsonHelper[A](val self: OWrites[A]) extends AnyVal {

    def narrowOWrites[B <: A]: OWrites[B] = self.contramap[B](identity)
  }


  implicit class FormatOpsPlayJsonHelper[A](val self: Format[A]) extends AnyVal {

    def narrowFormat[B <: A](implicit tag: ClassTag[B]): Format[B] = {
      Format(self.narrowReads[B], self.narrowWrites[B])
    }
  }


  implicit class OFormatOpsPlayJsonHelper[A](val self: OFormat[A]) extends AnyVal {

    def narrowOFormat[B <: A](implicit tag: ClassTag[B]): OFormat[B] = {
      OFormat(self.narrowReads[B], self.narrowOWrites[B])
    }
  }

  implicit class OFormatObjOpsPlayJsonHelper(val self: OFormat.type) extends AnyVal {

    def const[A](value: A): OFormat[A] = new OFormat[A] {

      def writes(a: A): JsObject = Json.obj()

      def reads(a: JsValue): JsResult[A] = a.validate[JsObject].map { _ => value }
    }

    def nested[A](name: String)(implicit format: Format[A]): OFormat[A] = new OFormat[A] {

      def writes(x: A): JsObject = Json.obj(name -> format.writes(x))

      def reads(json: JsValue): JsResult[A] = (json \ name).validate(format)
    }
  }


  implicit class EitherOpsPlayJsonHelper[L, R](val self: Either[L, R]) extends AnyVal {

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

  object DiscriminatedEitherFormat {
    def valueFromUnambiguousKey[A](key: String, otherKey: String)(implicit ra: Reads[A]): Reads[A] =
      new Reads[A] {
        def reads(js: JsValue) = js match {
          case o: JsObject =>
            o.value.get(key) match {
              case Some(value) =>
                if (o.value.contains(otherKey)) JsError(s"both discriminator keys received")
                else ra.reads(value)
              case None => JsError("no discriminator keys found")
            }
          case j => JsError(s"can't parse either for non-object for ${j}")
        }
      }

    def eitherReads[A, B](
      keyLeft: String,
      keyRight: String,
    )(
      implicit ra: Reads[A],
      rb: Reads[B],
    ): Reads[Either[A, B]] =
      Reads[Either[A, B]] { json =>
        valueFromUnambiguousKey[A](keyLeft, keyRight).reads(json).map(Left(_))
      }.orElse { json =>
        valueFromUnambiguousKey[B](keyRight, keyLeft).reads(json).map(Right(_))
      }

    def eitherWrites[A, B](
      keyLeft: String,
      keyRight: String,
    )(
      implicit wa: Writes[A],
      wb: Writes[B],
    ): OWrites[Either[A, B]] =
      OWrites[Either[A, B]] {
        case Left(a) => Json.obj(keyLeft -> wa.writes(a))
        case Right(b) => Json.obj(keyRight -> wb.writes(b))
      }

    def eitherFormat[A, B](
      keyLeft: String,
      keyRight: String,
    )(
      implicit fa: Format[A],
      fb: Format[B],
    ): OFormat[Either[A, B]] =
      OFormat.apply(eitherReads[A, B](keyLeft, keyRight), eitherWrites[A, B](keyLeft, keyRight))
  }

  trait TypeFormat[A] extends OFormat[A] {
    type Pf = PartialFunction[String, JsResult[A]]

    def readsPf(json: JsValue): Pf

    def reads(json: JsValue): JsResult[A] = {
      def reads(t: String, inner: JsObject) = {
        val pf = readsPf(inner)
        if (pf isDefinedAt t) pf(t)
        else JsError(s"No Reads defined for $t")
      }

      for {
        o <- json.validate[JsObject]
        typ <- (json \ "type").validate[String]
        result <- reads(typ, o - "type")
      } yield result
    }

    def writes(t: String, json: JsObject = Json.obj()): JsObject = {
      if (json.keys contains "type") sys error s"Inner JSON for '$t' subtype already contains 'type' field"
      Json.obj("type" -> t) ++ json
    }
  }


  object FoldedTypeFormat {

    def reads[A](readsPf: JsValue => PartialFunction[String, JsResult[A]]): Reads[A] = new Reads[A] {
      def reads(json: JsValue): JsResult[A] = {
        def reads(json: JsValue, t: String) = {
          val pf = readsPf(json)
          if (pf isDefinedAt t) pf(t)
          else JsError(s"No Reads defined for $t")
        }

        for {
          o <- json.validate[JsObject]
          typ <- (json \ "type").validate[String]
          inner = o - "type"

          result <- typ.span(_ != '#') match {
            case (t, "")   => reads(inner, t)
            case (t, rest) =>
              val j = inner ++ Json.obj("type" -> rest.tail)
              reads(j, t)
          }
        } yield result
      }
    }

    def writes[A](writesFunc: A => (String, JsObject)): OWrites[A] = new OWrites[A] {
      def writes(o: A): JsObject = {
        val (t, json) = writesFunc(o)

        (json \ "type").validate[String] match {
          case JsSuccess(typ, _) => json ++ Json.obj("type" -> s"$t#$typ")
          case _: JsError        => Json.obj("type" -> t) ++ json
        }
      }
    }
  }


  implicit def nelFormat[A: Format]: Format[Nel[A]] = new Format[Nel[A]] {

    def reads(json: JsValue): JsResult[Nel[A]] = for {
      list <- json.validate[List[A]]
      nel <- list match {
        case Nil          => JsError("list is empty")
        case head :: tail => JsSuccess(Nel(head, tail))
      }
    } yield nel

    def writes(x: Nel[A]): JsValue = Json toJson x.toList
  }


  implicit val UnitFormat: Format[Unit] = new Format[Unit] {

    def writes(o: Unit): JsValue = JsNull

    def reads(json: JsValue): JsResult[Unit] = json match {
      case JsNull => JsSuccess(())
      case _      => JsError("error.expected.jsnull")
    }
  }
}
