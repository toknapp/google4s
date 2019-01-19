---
layout: docs
title:  "KMS"
position: 2
sections: gkms
---

# Google4s-gkms

This project aims to provide easy access to [Google Cloud Key Management Service](https://cloud.google.com/kms/) services.
It is a wrapper of the official [google cloud storage java library](https://github.com/googleapis/google-cloud-java/tree/master/google-cloud-clients/google-cloud-kms)
abstracting over the tedious omniversal builder pattern and the ability to use arbitrary context for effect handling and concurrency.

- Effects and concurrency can be computed in an arbitrary type `F[_]`  
- A natural transformation `(() => ?) ~> F` have to be provided to lift the computation.


## Usage

Add the last stable version of **google4s-gkms** to your build dependencies:
 
```sbt
libraryDependencies += "co.upvest.google4s" %% "google4s-gkms" % "..."
```

**For more see [example](./example.html)**

You need to get a `Client` and provide it with the appropriate natural transformation
to lift your effect into `F`. The natural transformation have the form of `(() => ?) ~> F`
what in essence helps you to have more control over the effect by keeping it lazy. This is in particularly
helpful when used with strictly evaluated data structures as for example Scala's Future.

Some examples of the "Lifts" can be found in `co.upvest.google4s.core.Lifts`

## Limitations

- At the current state, the client does not support administrative
actions as Key or KeyRing creation as well as Key rotation is not supported programmatically. 
