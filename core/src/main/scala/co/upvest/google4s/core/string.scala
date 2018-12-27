package co.upvest.google4s.core

import java.nio.ByteBuffer
import java.nio.charset.{ CodingErrorAction, StandardCharsets }

import scala.util.Try

trait StringOps {
  private def strictDecoder() =
    StandardCharsets.UTF_8.newDecoder
      .onMalformedInput(CodingErrorAction.REPORT)

  implicit class UTFBytesToStringOps(bs: Array[Byte]) {
    def utf8: Try[String] = Try {
      strictDecoder().decode(ByteBuffer wrap bs).toString
    }
  }

  implicit class StringToUTFBytesOps(s: String) {
    def utf8: Array[Byte] = s.getBytes("UTF-8")
  }
}

object string extends StringOps
