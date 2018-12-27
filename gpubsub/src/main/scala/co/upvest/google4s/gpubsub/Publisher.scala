package co.upvest.google4s.gpubsub

import java.util.concurrent.TimeUnit

import akka.stream.scaladsl.{ Flow, Sink, Source }
import co.upvest.google4s.core.TryLift._
import co.upvest.google4s.core.{ Credentials, HostPort, Llift, Project }
import com.google.cloud.pubsub.v1.{ Publisher => JPublisher }
import com.google.pubsub.v1.ProjectTopicName

import scala.concurrent.duration.Duration
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success, Try }

object Publisher {

  type PublisherFlow[A] = Flow[A, Msg.Id, akka.NotUsed]

  final case class Config(
    topic: Topic,
    project: Project,
    parLevel: Int,
    usePlaintext: Boolean,
    credentials: Option[Credentials],
    host: Option[HostPort]
  ) extends PubSubConfig(
        project,
        usePlaintext,
        credentials,
        host
      ) {

    lazy val namePath = ProjectTopicName.of(project.s, topic.s)
  }

  /**
   * Produces an AKKA Stream Flow receiving `A` upstream
   * and sending Msg.Id of successfully published messages downstream.
   *
   * @param c Goocle cloud pubsub configuration.
   * @param ec Execution context for the produced Future.
   * @tparam A  Message that have to be published. Requiered to implement
   *            the Messagable typeclass.
   * @return AKKA Stream Flow
   */
  def flow[A: Messageable](c: Config)(
    implicit
    ec: ExecutionContext
  ): PublisherFlow[A] = future(c) match {
    case Success(f) =>
      Flow[A].mapAsyncUnordered(c.parLevel)(f(_))
    case Failure(t) =>
      Flow.fromSinkAndSource(
        Sink.ignore,
        Source.failed(t)
      )
  }

  /**
   * Convinience method for instantiating a Publisher
   * publishing inside a `scala.concurrent.Future`
   *
   * @param c Goocle cloud pubsub configuration.
   * @param ec Execution context for the produced Future.
   * @tparam A Message that have to be published. Requiered to implement
   *         the Messagable typeclass.
   * @return the Msg.Id of the published Message.
   */
  def future[A: Messageable](c: Config)(
    implicit
    ec: ExecutionContext
  ): Try[A => Future[Msg.Id]] =
    apply(c)(liftTry) map { p => (a: A) =>
      p.publish(a)
    }

  /**
   * Creates a Publisher client instance.
   * The instantiation can throw inside the
   * Java publisher implementation.
   * Ensure that the project exists on GCP and
   * the "GOOGLE_APPLICATION_CREDENTIALS" env variable
   * is set for a successful authentication.
   *
   * @param c Goocle cloud pubsub configuration.
   * @param lift Lift the instantiation in `F[_]`.
   * @tparam F Effect container.
   * @return Instance of Publisher
   */
  def apply[F[_]](c: Config)(lift: Llift[F]): F[Publisher] =
    lift { () =>
      val builder = JPublisher
        .newBuilder(c.namePath)
        .setChannelProvider(c.resolveChannelProvider)
        .setCredentialsProvider(c.resolveCredentials)

      new Publisher(builder.build)
    }
}

class Publisher private (p: JPublisher) {
  import co.upvest.google4s.core.GoogleConversions._

  /**
   * Publishes the given A.
   * This method transformes googles ApiFutre to ScalaFuture
   * where the callback is computed on the provided ExecutionContext.
   *
   * @param a entity to publish.
   * @param ec Execution Context for the callback of the async operation.
   * @return Id of the published message.
   */
  def publish[A: Messageable](a: A)(implicit ec: ExecutionContext): Future[Msg.Id] = p.publish(a).asScala.map { Msg.Id }

  /**
   * Publishes the given A.
   * This method blocks the ApiFuture returned by the publish call
   * this allows to do synchronous publishing or more controll over
   * the concurrency abstraction that can be provided with `F`.
   * When doing the latter, it is recomended to let the dispatcher
   * now that this is a blocking call, for example with
   * ```Future { blocking { ... } }```
   *
   * @param a entity to publish.
   * @param lift Natural transformation to `F`
   * @return Id of the published message.
   */
  def publish[A: Messageable, F[_]](a: A, lift: Llift[F]): F[Msg.Id] = lift { () =>
    Msg.Id(p publish a get)
  }

  /**
   *  Flushing the outstanding messages and blocks untill all are processed.
   *  New attempts to publish, after shutdown is invoked are rejected.
   */
  def shutdown[F[_]](timeout: Duration, lift: Llift[F]): F[Boolean] = lift { () =>
    p.shutdown()
    p.awaitTermination(
      timeout.toMillis,
      TimeUnit.MILLISECONDS
    )
  }
}
