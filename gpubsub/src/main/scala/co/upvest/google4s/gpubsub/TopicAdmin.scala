package co.upvest.google4s.gpubsub
import java.util.concurrent.TimeUnit

import cats.Monad
import cats.syntax.flatMap._
import cats.syntax.functor._
import co.upvest.google4s.core.{ Credentials, HostPort, Llift, Project }
import com.google.cloud.pubsub.v1.{ TopicAdminClient, TopicAdminSettings }
import com.google.pubsub.v1.{ ProjectName, ProjectTopicName }

import scala.concurrent.duration.{ Duration, _ }

object TopicAdmin {

  final case class Config(
    project: Project,
    usePlaintext: Boolean,
    credentials: Option[Credentials],
    host: Option[HostPort]
  ) extends PubSubConfig(
        project,
        usePlaintext,
        credentials,
        host
      )

  def apply[F[_]](c: Config)(lift: Llift[F]): F[TopicAdmin] =
    lift { () =>
      val builder = TopicAdminSettings
        .newBuilder()
        .setTransportChannelProvider(c.resolveChannelProvider)
        .setCredentialsProvider(c.resolveCredentials)

      new TopicAdmin(TopicAdminClient.create(builder.build), c.project)
    }

  /**
   * Helper to execute Topic administration tasks
   * with closing the GRPC channel after f is executed.
   * The shutdown timeout is static and should be potentially
   * extended in the config.
   */
  private def exec[F[_]: Monad, A](f: TopicAdmin => F[A], c: Config, lift: Llift[F]): F[A] = {
    val client = apply(c)(lift)
    client >>= f >>= { r =>
      client.flatMap {
        _.shutdown(5 second)(lift)
      } map { _ =>
        r
      }
    }
  }

  /**
   * Creates a new Topic, and close the GRPC channel after `f` is executed.
   * @param name have to be the plain TopicName
   * @param lift lifts the computation to be performed in `F`
   * @tparam F effect type.
   * @return FQDN of the topic deleted (projects/{project}/topics/{topic})
   */
  def createTopic[F[_]: Monad](c: Config, lift: Llift[F], name: Topic): F[String] =
    exec(_.createTopic(name)(lift), c, lift)

  /**
   * Deletes a existing Topic and close the GRPC channel after `f` is executed..
   * @param name have to be the plain TopicName
   * @param lift lifts the computation to be performed in `F`
   * @tparam F effect type.
   * @return FQDN of the topic deleted (projects/{project}/topics/{topic})
   */
  def deleteTopic[F[_]: Monad](c: Config, lift: Llift[F], name: Topic): F[String] =
    exec(_.deleteTopic(name)(lift), c, lift)
}

/**
 * The client entity should be used in case of recurring
 * administrative operations, since it keeps a persistent
 * connection via GRPC until `shutdown` is invoked.
 */
class TopicAdmin private (c: TopicAdminClient, p: Project) {
  import scala.collection.JavaConverters._

  /**
   * Creates a new Topic and leaves the GRPC channel open.
   * @param name have to be the plain TopicName
   * @param lift lifts the computation to be performed in `F`
   * @tparam F effect type.
   * @return FQDN of the topic created (projects/{project}/topics/{topic})
   */
  def createTopic[F[_]](name: Topic)(lift: Llift[F]): F[String] = lift { () =>
    c.createTopic(ProjectTopicName.of(p.s, name.s)).getName
  }

  /**
   * Deletes a existing Topic and leaves the GRPC channel open.
   * @param name have to be the plain TopicName
   * @param lift lifts the computation to be performed in `F`
   * @tparam F effect type.
   * @return FQDN of the topic deleted (projects/{project}/topics/{topic})
   */
  def deleteTopic[F[_]](name: Topic)(lift: Llift[F]): F[String] = lift { () =>
    val fqdn = ProjectTopicName.of(p.s, name.s)
    c.deleteTopic(fqdn)
    fqdn.toString
  }

  /**
   * Lists all avaliable topics
   * @param lift lifts the computation to be performed in `F`
   * @tparam F effect type.
   * @return List if FQDN of topics (projects/{project}/topics/{topic})
   */
  def listTopics[F[_]](lift: Llift[F]): F[List[String]] = lift { () =>
    c.listTopics(
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
