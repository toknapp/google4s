---
layout: docs
title:  "GoogleCloudStorage"
position: 4
sections: gstorage
---

# google4s-gstorage

This project aims to provide easy access to [Google Cloud Storage](https://cloud.google.com/storage/) services.
It is a wrapper of the official [google cloud storage java library](https://github.com/googleapis/google-cloud-java/tree/master/google-cloud-clients/google-cloud-storage)
abstracting over the tedious omniversal builder pattern and the ability to use arbitrary context for effect handling and concurrency.

- Effects and concurrency can be computed in an arbitrary type `F[_]`  
- A natural transformation `(() => ?) ~> F` have to be provided to lift the computation.

## Usage

Add the last stable version of **google4s-gstorage** to your build dependencies:
 
```sbt
libraryDependencies += "co.upvest.google4s" %% "google4s-gstorage" % "..."
```

You need to get a `Client` and provide it with the appropriate natural transformation
to lift your effect into `F`. The natural transformation have the form of `(() => ?) ~> F`
what in essence helps you to have more control over the effect by keeping it lazy. This is in particularly
helpful when used with strictly evaluated data structures as for example Scala's Future.

**For more see [example](./example.html)**


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
