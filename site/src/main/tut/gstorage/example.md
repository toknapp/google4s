---
layout: docs
title:  "Example"
sections: gstorage
---

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

