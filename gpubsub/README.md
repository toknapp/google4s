# google4s-gpubsub

This project aims to provide an easy access to [Google PubSub](https://cloud.google.com/pubsub/docs/) services.
It is a wrapper of the official [google pubsub java library](https://github.com/googleapis/google-cloud-java/tree/master/google-cloud-clients/google-cloud-pubsub).

The implementation of `Publisher` abstracts over Google Futures to either publish
to:
- A `Flow` in [Akka Streams](https://doc.akka.io/docs/akka/2.5/stream/).
- Scala's `Future` by converting the`Google Futures`
- An arbitary context `F[_]` by providing a natural transformation `(() => ?) ~> F` to lift the computation.

The implementation of `Subscriber` abstracts over the callbacks to:
- `Source` in context of [Akka Streams](https://doc.akka.io/docs/akka/2.5/stream/) utilizing
`akka.stream.scaladsl.SourceQueue` for providing backpressure.

The implementation of `TopicAdmin` and `SubscriptionAdmin` implementing topic and subscription administration methods. 

Encoding/Decoding of Messages are handled by the `gpubsub.Messageable` typeclass.
Some instances can be found in `co.upvest.google4s.gpubsub.Messagable.Converters`.

## Usage

Add the last stable version of **google4s-gpubsub** to your build dependencies:
 
```sbt
libraryDependencies += "co.upvest.google4s" %% "google4s-gpubsub" % "0.0.1"
```

## Examples

### Publishing

Lets publish some Blobs.
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

## Limitations
The current limitations and known issues are stated below. If you think
something is missed or have to be mentioned here please let us know.

- The streaming implementation is not abstract and uses AkkaStreams. 
Plan for the future is to make an abstract specification and add
implementations for scalaz streams and fs2.  

- The potential configurations of google libraries can be incredibly 
versatile and rich on options, those can be confusing when reading source code due to constructor overloading
mixed with builder pattern you see everywhere in the Java implementation. We tried to keep the configuration
as lean as possible so we might miss some important things, let 
use know if you miss something.


## Contact

By questions, comments or suggestions feel free to get in touch by creating a PR, issue or telepathically. 


### Cavecats [sic](https://www.youtube.com/watch?v=a0SuhNn8S60) 

Copyright 2018 Ivan Morozov, Gustav Behm, Tokn GmbH (https://upvest.co)

**google4s-gstorage** is provided to you as free software under the MIT license.
The MIT software license is attached in the [COPYING](/../COPYING) file.