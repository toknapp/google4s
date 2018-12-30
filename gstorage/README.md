# google4s-gstorage

This project aims to provide easy access to [Google Cloud Storage](https://cloud.google.com/storage/) services.
It is a wrapper of the official [google cloud storage java library](https://github.com/googleapis/google-cloud-java/tree/master/google-cloud-clients/google-cloud-storage)
abstracting over the tedious omniversal builder pattern and the ability to use arbitrary context for effect handling and concurrency.

- Effects and concurrency can be computed in an arbitrary type `F[_]`  
- A natural transformation `(() => ?) ~> F` have to be provided to lift the computation.

Additionally to the original cloud storage implementation, this project utilizes:
 - [Cats](https://github.com/typelevel/cats) for FP fuzz..
 - [Kind Projector](https://github.com/non/kind-projector) because we can.

## Usage

Add the last stable version of **google4s-gstorage** to your build dependencies:
 
```sbt
libraryDependencies += "co.upvest.google4s" %% "google4s-gstorage" % "0.0.2"
```

You need to get a `Client` and provide it with the appropriate natural transformation
to lift your effect into `F`. The natural transformation have the form of `(() => ?) ~> F`
what in essence helps you to have more control over the effect by keeping it lazy. This is in particularly
helpful when used with strictly evaluated data structures as for example Scala's Future.

Some examples of the "Lifts" can be found in `co.upvest.google4s.core.Lifts`

## Examples

### Create the Client

Lets create some clients computing in different `F`.
```scala
    import co.upvest.google4s.gstorage.Client
    import co.upvest.google4s.core._
    import co.upvest.google4s.gstorage._
    import scala.concurrent.ExecutionContext.Implicits.global
    
    // Ensure that the project exists and you are authenticated.
    // The easiest way to authenticate is by setting the "GOOGLE_APPLICATION_CREDENTIALS" 
    // environment variable. More info can be found at:
    // https://cloud.google.com/docs/authentication/getting-started 
    val config = Client.Config("google_project_name")

    // Now, the effect computation happens in cats.Id 
    val clientId = Client(config)(IdLift.liftId)
        
    // Now, the effect computation happens in Future
    val clientFuture = Client(config)(FutureLift.liftWithEC)    
``` 

### Put 
If a `Bucket` or a `Blob` with this name does not exist - it will be created or updated.
```scala
  val payloadId = Blob.Id(BucketName("lalala"), BlobName("lololo"))
  
  val importantPayload = Array.fill(23)(0.toByte)
  
  // Up it goes... 
  val blob = clientId.put(payloadId, importantPayload)
  
  // update your blob direct with the blob
  val updated = blob.update(...)
  
  import cats.syntax.FlatMapSyntax._
  import cats.instances.future._
  
  // Or just sequence operations by using an F that allows it. 
  clientFuture.put(id, importantPayload) >> clientFuture.put(id, ...)
``` 

### Get
Fetching blobs.
```scala
  // This result does not contain the payload and will be None 
  // if nothing can be found.
  val maybeBlob: Future[Option[Blob[Future]]] = clientFuture.get(payloadId)
  
  // The payload is lazy and have to be fetched separately with `_.data()`
  val payload = maybeBlob >>= {
   
    // This is only one (and not the nicest!) way to get around the F[Option[F]]] type result
    // cats provide diverse monad transformers to write this in a cleaner fashion.
    
    case Some(b) => b.data()
    case None => Future.failed(...)
  }
```

### List 
We can search through our blobs and buckets by 
using filter conditions

```scala
    // We will get all the blobs located in bucket
    val blobs: Future[List[Blob.Id]] = clientFuture.list(BucketName("bucket"), None)
    
    // Return all the blobs containing the prefix "prefix/"
    val blobsFiltered: Future[List[Blob.Id]] = clientFuture
      .list(
        BucketName("lalala"),
        BlobName("prefix/")
      )
```


## Limitations
The current limitations and known issues are stated below. If you think
something is missed or have to be mentioned here please let us know.

- At the current state, the project does not support advanced filters 
e.g combinations of blob and bucket prefixes etc...

- Listing of findings is strict, that mean you potentially could run into
memory problems by "finding" large results. The underlying implementation
uses iterators so a stream implementation should be straightforward. 

- The value classes e.g `BucketName` could be a potential performance penalty caused
by wrapping/unwrapping when working with large lists. It should be potentially replaced
with a nice macro or some tagged types. 

## Contact

By questions, comments or suggestions feel free to get in touch by creating a PR, issue or telepathically. 


### Cavecats [sic](https://www.youtube.com/watch?v=a0SuhNn8S60) 

Copyright 2019 Ivan Morozov, Gustav Behm, Tokn GmbH (https://upvest.co)

**google4s-gstorage** is provided to you as free software under the MIT license.
The MIT software license is attached in the [COPYING](/../COPYING) file.