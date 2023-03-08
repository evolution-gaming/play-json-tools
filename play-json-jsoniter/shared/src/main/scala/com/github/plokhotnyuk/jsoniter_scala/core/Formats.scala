package com.github.plokhotnyuk.jsoniter_scala.core

import play.api.libs.json._

import java.nio.charset.StandardCharsets
import scala.util.control.NonFatal

/** INTERNAL API
  *
  * It is an internal implementation for [[com.evolution.playjson.jsoniter.PlayJsonJsoniter]]
  */
object Formats {
  private[this] val pool = new ThreadLocal[(Array[Byte], JsonReader, JsonWriter)] {
    override def initialValue(): (Array[Byte], JsonReader, JsonWriter) = {
      val buf = new Array[Byte](128)
      (buf, new JsonReader(buf, charBuf = new Array[Char](128)), new JsonWriter(buf))
    }
  }

  def smallAsciiStringFormat[A](name: String, f: (JsonReader, A) => A, g: (JsonWriter, A) => Unit): Format[A] =
     new JsonValueCodec[A] with Format[A] {
      override def reads(json: JsValue): JsResult[A] =
        try {
          val (buf, reader, _) = pool.get
          buf(0) = '"'
          val s = json.asInstanceOf[JsString].value
          val len = s.length
          var bits, i = 0
          while (i < len) {
            val ch = s.charAt(i)
            i += 1
            buf(i) = ch.toByte
            bits |= ch
          }
          buf(i + 1) = '"'
          if (bits >= 0x80) JsError(name)
          else new JsSuccess(reader.read(this, buf, 0, len + 2, ReaderConfig))
        } catch {
          case NonFatal(_) => JsError(name)
        }

      override def writes(x: A): JsValue = {
        val (buf, _, writer) = pool.get
        val len = writer.write(this, x, buf, 0, buf.length, WriterConfig)
        new JsString(new String(buf, 1, len - 2, StandardCharsets.UTF_8))
      }

      override def decodeValue(in: JsonReader, default: A): A = f(in, default)

      override def encodeValue(x: A, out: JsonWriter): Unit = g(out, x)

      override def nullValue: A = null.asInstanceOf[A]
    }
}
