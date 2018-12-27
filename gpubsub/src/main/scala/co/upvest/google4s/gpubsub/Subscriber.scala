package co.upvest.google4s.gpubsub

import akka.NotUsed
import akka.stream.scaladsl.{ Merge, Source, SourceQueueWithComplete }
import akka.stream.{ Materializer, OverflowStrategy, QueueOfferResult }
import co.upvest.google4s.core.{ Credentials, HostPort, Project }
import com.google.api.core.ApiService
import com.google.cloud.pubsub.v1.{ AckReplyConsumer, MessageReceiver, Subscriber => JSubscriber }
import com.google.pubsub.v1.{ ProjectSubscriptionName, PubsubMessage }
import org.threeten.bp.{ Duration => GDuration }

import scala.concurrent.duration.{ Duration, FiniteDuration }
import scala.concurrent.{ Await, ExecutionContext, ExecutionContextExecutor }

/**
 * Subscribes the given Subscription
 */
object Subscriber {

  final case class Config(
    project: Project,
    usePlaintext: Boolean,
    subscriptionId: Subscription,
    maxAckDuration: FiniteDuration,
    bufferSize: Int,
    strategy: OverflowStrategy,
    parallelPulls: Int = 1,
    credentials: Option[Credentials],
    host: Option[HostPort]
  ) extends PubSubConfig(project, usePlaintext, credentials, host) {

    lazy val name = ProjectSubscriptionName.of(project.s, subscriptionId.s)
  }

  def apply(c: Config)(
    implicit
    ec: ExecutionContext,
    mat: Materializer
  ): Source[Msg, NotUsed] =
    if (c.parallelPulls == 1) {
      mkSource(c)
    } else {
      val s0 :: s1 :: ss = List.fill(c.parallelPulls)(mkSource(c))
      Source.combine(s0, s1, ss: _*)(Merge(_))
    }

  private def mkSource(c: Config)(
    implicit
    ec: ExecutionContext,
    mat: Materializer
  ): Source[Msg, NotUsed] = {
    val (q, s) = Source.queue[Msg](c.bufferSize, c.strategy).preMaterialize

    val r   = mkMessageReceiver(c, q)
    val l   = mkListener(q)
    val sub = mkSubscriber(c, r)
    sub.startAsync()
    sub.addListener(
      l,
      ec.asInstanceOf[ExecutionContextExecutor]
    )

    s
  }

  private def mkMessageReceiver(
    config: Config,
    q: SourceQueueWithComplete[Msg]
  )(implicit ec: ExecutionContext) = new MessageReceiver {
    override def receiveMessage(m: PubsubMessage, c: AckReplyConsumer): Unit =
      q.synchronized {
        Await.result(
          q.offer(
            new Msg(m, c, Subscription(config.name.toString))
          ) map {
            case QueueOfferResult.Enqueued =>
            // Successfully enqueued
            case QueueOfferResult.QueueClosed =>
            // Stream completed
            case QueueOfferResult.Dropped =>
              // Queue dropped the message, nack to try again.
              c.nack()
            case QueueOfferResult.Failure(t) =>
              // Failure in stream or during the call.
              c.nack()
              q.fail(t)
          },
          Duration.Inf
        )
      }
  }

  private def mkListener(q: SourceQueueWithComplete[_]) =
    new ApiService.Listener {
      override def failed(from: ApiService.State, t: Throwable): Unit =
        q.fail(t)
    }

  private def mkSubscriber(c: Config, mr: MessageReceiver) =
    JSubscriber
      .newBuilder(c.name, mr)
      .setChannelProvider(c.resolveChannelProvider)
      .setCredentialsProvider(c.resolveCredentials.cp)
      .setMaxAckExtensionPeriod(
        GDuration.ofMillis(c.maxAckDuration.toMillis)
      )
      .setParallelPullCount(1)
      // Enforcing parallel pull count = 1 here is important when using the
      // backpressure strategy:
      // https://github.com/akka/akka/blob/b4f799a7a9096f6bd1ef57335afa903799fa3492/akka-stream/src/main/scala/akka/stream/scaladsl/Queue.scala#L24
      // """
      //  Additionally when using the backpressure overflowStrategy:
      //    - If the buffer is full the Future won't be completed until
      //      there is space in the buffer
      //    - Calling offer before the Future is completed in this case
      //      will return a failed Future
      // """
      // So per queue we only want to pull one message at a time
      .build
}
