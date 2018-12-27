package co.upvest.google4s.gpubsub

import java.util.concurrent.TimeUnit

import co.upvest.google4s.core.{ Credentials, HostPort, Llift, Project }
import com.google.cloud.pubsub.v1.{ SubscriptionAdminClient, SubscriptionAdminSettings }
import com.google.pubsub.v1.{ ProjectName, ProjectSubscriptionName, ProjectTopicName, PushConfig }

import scala.concurrent.duration.{ Duration, FiniteDuration }

object SubscriptionAdmin {

  private final val NoPush = ""

  final case class Config(
    project: Project,
    usePlaintext: Boolean,
    credentials: Option[Credentials],
    host: Option[HostPort],
    // If set to None, `Pull` is used.
    pushEndpoint: Option[PushEndpoint],
    ackDeadline: FiniteDuration
  ) extends PubSubConfig(
        project,
        usePlaintext,
        credentials,
        host
      )

  def apply[F[_]](c: Config)(lift: Llift[F]): F[SubscriptionAdmin] =
    lift { () =>
      val builder = SubscriptionAdminSettings
        .newBuilder()
        .setTransportChannelProvider(c.resolveChannelProvider)
        .setCredentialsProvider(c.resolveCredentials)

      new SubscriptionAdmin(
        SubscriptionAdminClient.create(builder.build),
        c.project,
        PushConfig
          .newBuilder()
          .setPushEndpoint(
            c.pushEndpoint
              .map(_.s)
              .getOrElse(NoPush)
          )
          .build(),
        c.ackDeadline.toSeconds.toInt
      )
    }
}

/**
 * The client entity should be used in case of recurring
 * administrative operations, since it keeps a persistent
 * connection via GRPC until `shutdown` is invoked.
 */
class SubscriptionAdmin private (c: SubscriptionAdminClient, p: Project, pC: PushConfig, ackSec: Int) {
  import scala.collection.JavaConverters._

  /**
   * Creates a new Topic and leaves the GRPC channel open.
   *
   * @param name have to be the plain TopicName
   * @param lift lifts the computation to be performed in `F`
   * @tparam F effect type.
   * @return FQDN of the topic created (projects/{project}/topics/{topic})
   */
  def createSubscription[F[_]](name: Subscription, topic: Topic)(lift: Llift[F]): F[String] = lift { () =>
    val sub = ProjectSubscriptionName.of(p.s, name.s)
    val top = ProjectTopicName.of(p.s, topic.s)
    c.createSubscription(sub, top, pC, ackSec).getName
  }

  /**
   * Deletes a existing Topic and leaves the GRPC channel open.
   *
   * @param name have to be the plain TopicName
   * @param lift lifts the computation to be performed in `F`
   * @tparam F effect type.
   * @return FQDN of the subscriptions deleted (projects/{project}/subscriptions/{subscription})
   */
  def deleteSubscription[F[_]](name: Subscription)(lift: Llift[F]): F[String] = lift { () =>
    val sub = ProjectSubscriptionName.of(p.s, name.s)
    c.deleteSubscription(sub)
    sub.toString
  }

  /**
   * List the avaliable subscriptions.
   *
   * @param lift lifts the computation to be performed in `F`
   * @tparam F effect type.
   * @return FQDN of avaliable subscriptions (projects/{project}/subscriptions/{subscription})
   */
  def listSubscriptions[F[_]](lift: Llift[F]): F[List[String]] = lift { () =>
    c.listSubscriptions(
        ProjectName.of(p.s)
      )
      .iterateAll()
      .iterator()
      .asScala
      .map(_.getName)
      .to[List]
  }

  /**
   * Flushing the outstanding work and blocks untill all work is done
   * new work is rejected. Can be interrupted and fail.
   *
   * @param timeout time to finish work and shutdown before return false.
   * @param lift lifts the computation to be performed in `F`.
   * @tparam F effect type.
   * @return true if successful shutdown in given time false if not.
   */
  def shutdown[F[_]](timeout: Duration)(lift: Llift[F]): F[Boolean] = lift { () =>
    c.shutdown()
    c.awaitTermination(
      timeout.toMillis,
      TimeUnit.MILLISECONDS
    )
  }
}
