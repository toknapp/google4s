package co.upvest.google4s.gpubsub

import akka.stream.scaladsl.Sink
import akka.testkit.{ AkkaSpec, TestProbe }
import co.upvest.google4s.core.IdLift

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class SubscriberSourceTest extends AkkaSpec with Messageable.Converters with Messageable.Syntax {
  import PubSubTestUtil._
  implicit val ec: ExecutionContext = system.dispatcher

  override def atStartup(): Unit =
    preparePubSub(TestTopic, TestSubscription)

  case class Blob(blub: String)

  implicit val converter = Messageable.of[Blob](
    b => fromBytes.asMsg(b.blub.getBytes),
    pm => Blob(pm.getData.toStringUtf8)
  )

  lazy val subSource = Subscriber(SubscriberConfig)

  lazy val publisher = Publisher(PublisherConfig)(IdLift.liftId)

  "A subscriber source" should {
    "receive all published messages in" in {

      val probe = TestProbe()

      val messages = List(Blob("pub1"), Blob("pub2"), Blob("pub3"))

      val testSource = subSource.map { pm =>
        val result = pm.as[Blob]
        pm.ack().get
        result
      }.to(Sink.actorRef(probe.ref, "subSource completed")).run()

      messages foreach { publisher.publish(_, IdLift.liftId) }

      probe.expectMsgAllOf(10 second, messages: _*)

    }
  }
}
