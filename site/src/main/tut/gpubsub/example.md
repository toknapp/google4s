---
layout: docs
title:  "Example"
sections: gpubsub
---

### Publishing

Let's publish some Blobs.

```scala
    import co.upvest.google4s.gpubsub.Publisher
    import co.upvest.google4s.gpubsub.Messageable
    // Some instances of Messageble
    import co.upvest.google4s.gpubsub.instances._
    import co.upvest.google4s.core._
    import akka.stream.scaladsl.{Source, Sink}

    case class Blob(s: String)
    
    // Here the decoder/encoder typeclass.
    implicit val blobConverter = Messageable.of[Blob](
      b => fromBytes.asMsg(b.blub.getBytes),
      pm => Blob(pm.getData.toStringUtf8)
  )
  
    // Config
    val c: Publisher.Config = Publisher.Config(...)
  
    // Create a Flow
    val pFlow = Publisher.flow[Blob](c)
    // or a Client
    val client = Publisher(c)(IdLift.liftId)
    
    // the flow constructor finds the implicit in the context
    // to convert the Blobs.
    Source(Blob("a") :: Blob("b") :: Nil).via(pFlow).to(Sink.seq)
    
    // or publish directly from client in F of your choice.
    client.publish(Blob("bolb"), IdLift.liftId)
```

 
### Subscribing

```scala
import co.upvest.google4s.gpubsub.Subscriber
import akka.stream.scaladsl.Sink

val config: Subscriber.Config = Subscriber.Config(...)

// Create a source
val source = Subscriber(config)

// Run the source into a flow/sink of choice.
source.runWith(Sink.foreach(println))

```