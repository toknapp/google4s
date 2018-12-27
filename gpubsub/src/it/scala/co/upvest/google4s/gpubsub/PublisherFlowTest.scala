package co.upvest.google4s.gpubsub

import akka.stream.scaladsl.{ Sink, Source }
import akka.testkit.AkkaSpec
import org.scalatest.prop.GeneratorDrivenPropertyChecks

import scala.concurrent.duration._
import scala.concurrent.{ Await, ExecutionContext }


class PublisherFlowTest
    extends AkkaSpec
    with GeneratorDrivenPropertyChecks
    with Arbitrarys
    with Messageable.Converters {

  import PubSubTestUtil._

  implicit val ec: ExecutionContext = system.dispatcher

  override def atStartup(): Unit =
    preparePubSub(TestTopic, TestSubscription)

  lazy val pFlow = Publisher.flow[Array[Byte]](PublisherConfig)

  "A publisher flow" should {
    "publish messages" in {
      forAll(nonEmptyPayloadSeqs) { (payloads: List[Array[Byte]]) =>
        val res = Source(payloads)
          .via(pFlow)
          .runWith(Sink.fold(List.empty[Msg.Id])((a, b) => b :: a))

        Await.result(res, 3.seconds).size shouldBe payloads.size
      }
    }

    "fail on empty payloads" in {

      val res = Source(List(Array.emptyByteArray))
        .via(pFlow)
        .runWith(Sink.fold(List.empty[Msg.Id])((a, b) => b :: a))

      an[com.google.api.gax.rpc.InvalidArgumentException] should be thrownBy
        Await.result(res, 3.seconds)
    }
  }
}
