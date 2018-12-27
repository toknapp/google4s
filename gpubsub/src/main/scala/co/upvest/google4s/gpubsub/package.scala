package co.upvest.google4s

import java.time.Instant

import com.google.cloud.pubsub.v1.AckReplyConsumer
import com.google.pubsub.v1.PubsubMessage
import co.upvest.google4s.core.string.UTFBytesToStringOps
import cats.syntax.flatMap._
import cats.instances.try_._
import co.upvest.google4s.core.{ Credentials, HostPort, Project }
import com.google.api.gax.grpc.GrpcTransportChannel
import com.google.api.gax.rpc.FixedTransportChannelProvider
import com.google.cloud.pubsub.v1.stub.PublisherStubSettings
import io.grpc.ManagedChannelBuilder

import scala.collection.JavaConverters._
import scala.util.Try

package object gpubsub {

  object instances extends Messageable.Converters
  object syntax    extends Messageable.Syntax

  final case class PushEndpoint(s: String) extends AnyVal

  final case class Subscription(s: String) extends AnyVal

  final case class Topic(s: String) extends AnyVal

  abstract class PubSubConfig(p: Project, plain: Boolean, c: Option[Credentials], h: Option[HostPort]) {

    def resolveChannelProvider: FixedTransportChannelProvider = {

      val cb_ = ManagedChannelBuilder.forTarget(resolveEndpoint)

      // _.usePlaintext(...) is depricated.
      val cb = if (plain) cb_.usePlaintext else cb_

      FixedTransportChannelProvider
        .create(GrpcTransportChannel.create(cb.build))
    }

    def resolveCredentials: Credentials =
      c.getOrElse(Credentials.defaultCredentials)

    def resolveEndpoint: String =
      h.map(_.s)
        .getOrElse(PublisherStubSettings.getDefaultEndpoint)
  }

  class Msg(
    val msg: PubsubMessage,
    consumer: AckReplyConsumer,
    subscription: Subscription
  ) {

    lazy val data: Option[Array[Byte]] =
      Option(msg.getData.toByteArray)

    lazy val asUtf8String: Try[String] = Try { data.get } >>= { _.utf8 }

    lazy val id: Msg.Id = Msg.Id(msg.getMessageId)

    lazy val publishedAt: Instant =
      Instant.ofEpochSecond(msg.getPublishTime.getSeconds)

    lazy val attributes: Msg.Attributes = msg.getAttributesMap.asScala.toMap

    def ack(): Try[Unit] = Try { consumer.ack() }

    def nack(): Try[Unit] = Try { consumer.nack() }
  }

  object Msg {

    type Attributes = Map[String, String]

    final case class Id(s: String) extends AnyVal
  }
}
