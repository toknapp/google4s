# google4s-gkms

This project aims to provide easy access to [Google Cloud Key Management Service](https://cloud.google.com/kms/) services.
It is a wrapper of the official [google cloud storage java library](https://github.com/googleapis/google-cloud-java/tree/master/google-cloud-clients/google-cloud-kms)
abstracting over the tedious omniversal builder pattern and the ability to use arbitrary context for effect handling and concurrency.

- Effects and concurrency can be computed in an arbitrary type `F[_]`  
- A natural transformation `(() => ?) ~> F` have to be provided to lift the computation.

Additionally to the original cloud storage implementation, this project utilizes:
 - [Cats](https://github.com/typelevel/cats) for FP fuzz..
 - [Kind Projector](https://github.com/non/kind-projector) because we can.
 - [Terminology](https://github.com/toknapp/terminology) for tagging primitives.

## Usage

Add the last stable version of **google4s-gkms** to your build dependencies:
 
```sbt
libraryDependencies += "co.upvest.google4s" %% "google4s-gkms" % "0.0.1"
```

You need to get a `Client` and provide it with the appropriate natural transformation
to lift your effect into `F`. The natural transformation have the form of `(() => ?) ~> F`
what in essence helps you to have more control over the effect by keeping it lazy. This is in particularly
helpful when used with strictly evaluated data structures as for example Scala's Future.

Some examples of the "Lifts" can be found in `co.upvest.google4s.core.Lifts`

## Examples

### Encrypt/Decrypt

```scala
    
    import co.upvest.google4s.core._
    import co.upvest.google4s.gkms._
    import co.upvest.terminology.adjectives.common._
    
    // Ensure that the project exists and you are authenticated.
    // The easiest way to authenticate is by setting the "GOOGLE_APPLICATION_CREDENTIALS" 
    // environment variable. More info can be found at:
    // https://cloud.google.com/docs/authentication/getting-started 
    val config = Client.Config(...)

    // Now, the effect computation happens in cats.Id 
    val clientId = Client(config)(IdLift.liftId)
    
    val payload = "blob".getBytes
        
    // The key have to exist
    // https://cloud.google.com/kms/docs/creating-asymmetric-keys
    val key = Key(...)
    val encrypted = clientId.encrypt(PlainText(payload))
    
    val decrypted = clientId.decrypt(encrypted, key)
    
    assert(decrypted.t.deep == payload.deep)         
``` 

## Limitations
The current limitations and known issues are stated below. If you think
something is missed or have to be mentioned here please let us know.

- At the current state, the client does not support administrative
actions as Key or KeyRing creation as well as Key rotation is not supported programmatically. 

## Contact

By questions, comments or suggestions feel free to get in touch by creating a PR, issue or telepathically. 


### Cavecats [sic](https://www.youtube.com/watch?v=a0SuhNn8S60) 

Copyright 2019 Ivan Morozov, Gustav Behm, Tokn GmbH (https://upvest.co)

**google4s-gstorage** is provided to you as free software under the MIT license.
The MIT software license is attached in the [COPYING](/../COPYING) file.