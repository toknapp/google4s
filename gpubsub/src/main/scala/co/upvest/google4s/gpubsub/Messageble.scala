package co.upvest.google4s.gpubsub
import co.upvest.google4s.gpubsub.Msg.Attributes
import com.google.protobuf.ByteString
import com.google.pubsub.v1.PubsubMessage
import akka.util.{ ByteString => AkkaByteString }
import scala.collection.JavaConverters._

trait Messageable[A] extends (A => PubsubMessage) {

  def asMsg(a: A): PubsubMessage

  def as(m: PubsubMessage): A
}

object Messageable {

  def of[A](f: A => PubsubMessage, g: PubsubMessage => A) = new Messageable[A] {
    def apply(a: A): PubsubMessage          = f(a)
    override def asMsg(a: A): PubsubMessage = apply(a)
    override def as(m: PubsubMessage): A    = g(m)
  }

  implicit def apply[A](implicit m: Messageable[A]) = m

  trait Syntax {
    implicit class AsMsgSyntax[A](a: A) {
      def asMsg(implicit m: Messageable[A]): PubsubMessage = m.asMsg(a)
    }

    implicit class FromMsgSyntax(pm: PubsubMessage) {
      def as[A](implicit m: Messageable[A]): A = m.as(pm)
    }

    implicit class FromCMsgSyntax(pm: Msg) {
      def as[A](implicit m: Messageable[A]): A = m.as(pm.msg)
    }
  }

  trait Converters {

    implicit val fromBytes = Messageable.of[Array[Byte]](
      b =>
        PubsubMessage
          .newBuilder()
          .setData(ByteString.copyFrom(b))
          .build,
      m => m.getData.toByteArray
    )

    implicit val fromByteString = Messageable.of[AkkaByteString](
      bs => fromBytes.asMsg(bs.toArray[Byte]),
      m => AkkaByteString(fromBytes.as(m))
    )

    implicit val fromBytesAndAttrs = Messageable.of[(Array[Byte], Attributes)](
      {
        case (b, attrs) =>
          PubsubMessage
            .newBuilder()
            .setData(ByteString.copyFrom(b))
            .putAllAttributes(attrs.asJava)
            .build
      },
      m =>
        fromBytes.as(m) ->
          m.getAttributesMap.asScala.toMap
    )
  }
}
