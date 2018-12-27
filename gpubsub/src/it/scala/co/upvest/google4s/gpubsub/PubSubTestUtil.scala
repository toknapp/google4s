package co.upvest.google4s.gpubsub

import akka.stream.OverflowStrategy
import co.upvest.google4s.core.IdLift._
import co.upvest.google4s.core.{Credentials, HostPort, Project}

import scala.concurrent.duration._

object PubSubTestUtil {

  val TestCredentials = Some(Credentials.noCredentials)

  lazy val TestHost = sys.env
    .get("PUBSUB_TEST_HOST") map HostPort orElse Some(HostPort("localhost:8086"))

  lazy val TestProject = sys.env
    .get("PUBSUB_TEST_PROJECT") map Project getOrElse Project("Test_Project")

  lazy val TestTopicAdminConfig = TopicAdmin
    .Config(TestProject, true, TestCredentials, TestHost)

  lazy val TestSubsAdminConfig = SubscriptionAdmin
    .Config(TestProject, true, TestCredentials, TestHost, None, 10 second)

  lazy val TestTopic = Topic("topic_test")

  lazy val TestSubscription = Subscription("subscription_test")

  lazy val PublisherConfig = Publisher.Config(
    topic = TestTopic,
    project = TestProject,
    parLevel = 1,
    usePlaintext = true,
    credentials = TestCredentials,
    host = TestHost
  )

  lazy val SubscriberConfig = Subscriber.Config(
    project = TestProject,
    subscriptionId = TestSubscription,
    maxAckDuration = 10 second,
    bufferSize = 10,
    parallelPulls = 1,
    usePlaintext = true,
    credentials = TestCredentials,
    host = TestHost,
    strategy = OverflowStrategy.fail
  )

  def preparePubSub(topic: Topic, sub: Subscription): Unit = {
    // Create Topic of not existing.

    val subAdmin =
      SubscriptionAdmin(TestSubsAdminConfig)(liftId)

    val topicAdmin =
      TopicAdmin(TestTopicAdminConfig)(liftId)

    topicAdmin
      .listTopics(liftId)
      .filter(_.contains(topic.s))
      .foreach { _ =>
        topicAdmin.deleteTopic(topic)(liftId)
      }
    topicAdmin.createTopic(topic)(liftId)

    subAdmin
      .listSubscriptions(liftId)
      .filter(_.contains(sub.s))
      .foreach { _ =>
        subAdmin
          .deleteSubscription(sub)(liftId)
      }

    subAdmin.createSubscription(sub, topic)(liftId)
    topicAdmin.shutdown(5 second)(liftId)
    subAdmin.shutdown(5 seconds)(liftId)
  }

}
