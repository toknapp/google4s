---
layout: docs
title:  "PubSub"
position: 3
sections: gpubsub
---

# google4s-gpubsub

This project aims to provide easy access to [Google PubSub](https://cloud.google.com/pubsub/docs/) services.
It is a wrapper of the official [google PubSub Java library](https://github.com/googleapis/google-cloud-java/tree/master/google-cloud-clients/google-cloud-pubsub).

The implementation of `Publisher` abstracts over Google Futures to either publish
to:
- A `Flow` in [Akka Streams](https://doc.akka.io/docs/akka/2.5/stream/).
- Scala's `Future` by converting the`Google Futures`
- An arbitrary context `F[_]` by providing a natural transformation `(() => ?) ~> F` to lift the computation.

The implementation of `Subscriber` abstracts over the callbacks to:
- `Source` in context of [Akka Streams](https://doc.akka.io/docs/akka/2.5/stream/) utilizing
`akka.stream.scaladsl.SourceQueue` for providing backpressure.

The implementation of `TopicAdmin` and `SubscriptionAdmin` implementing topic and subscription administration methods. 

Encoding/Decoding of Messages is handled by the `gpubsub.Messageable` type class.
Some instances can be found in `co.upvest.google4s.gpubsub.Messagable.Converters`.

## Usage

Add the last stable version of **google4s-gpubsub** to your build dependencies:
 
```sbt
libraryDependencies += "co.upvest.google4s" %% "google4s-gpubsub" % "..."
```

**For more see [example](./example.html)**

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
