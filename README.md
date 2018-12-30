# Google4s

[![Build Status](https://circleci.com/gh/toknapp/google4s.svg?style=svg)](https://circleci.com/gh/toknapp/google4s)
[![Latest version](https://index.scala-lang.org/toknapp/google4s/google4s-gkms/latest.svg?color=orange)](https://index.scala-lang.org/toknapp/google4s/google4s-gkms)
[![Latest version](https://index.scala-lang.org/toknapp/google4s/google4s-gpubsub/latest.svg?color=orange)](https://index.scala-lang.org/toknapp/google4s/google4s-gpubsub)
[![Latest version](https://index.scala-lang.org/toknapp/google4s/google4s-gstorage/latest.svg?color=orange)](https://index.scala-lang.org/toknapp/google4s/google4s-gstorage)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](COPYING)



This project aims to provide safe, composable and easy to use clients for [Google Cloud Platform Services](https://github.com/googleapis/google-cloud-java) written
in functional style **Scala**.

Currently supported clients are

- [Google Cloud Storage](gstorage/README.md)

- [Google Cloud PubSub](gpubsub/README.md)

- [Google Cloud Key Management Service (KMS)](gkms/README.md)


The client implementations are using Google's Java clients underneath
however, the Java code is mostly generated so it's not really fun to use it.

 - Omniversal nested builder pattern
 - No or limited concurrency
 - Uncomposable patterns 
 
`google4s` solves this problems by replacing the builder pattern with simple
method calls with strong data types. Effect handling and concurrency can be
done in an arbitary kind `F[_]` you just have to provide the natural transformation:
```scala
(() => ?) ~> F
``` 
See the `Llift` type [for deails](core/src/main/scala/co/upvest/google4s/core/package.scala).

## Usage
The clients are structured and released as separate modules.

Add the latest version to your project

### Latest Stable
```sbt
libraryDependencies += "co.upvest.google4s" %% "google4s-..."  % "0.0.2"
```
### Latest Snapshot
```sbt
libraryDependencies += "co.upvest.google4s" %% "google4s-..."  % "0.0.2-SNAPSHOT"
```

## Examples
Please refer to the specific client documentation for examples:
- [Google Storage](gstorage/README.md)
- [Google PubSub](gpubsub/README.md) 
- [Google KMS](gkms/README.md).

## Contributing

Coming soon... PR's are welcome and highly appreciated!

## Limitations

- We try to implement the configs as lean as possible. As the result some advanced
configuration could be missing. If you encounter this problem **please create an issue or a PR**
we are happy to add what is missing.

- Please refer to the specific client documentation for the list of known limitations:
    - [Google Storage](gstorage/README.md)
    - [Google PubSub](gpubsub/README.md) 
    - [Google (KMS)](gkms/README.md).
   

## Contact

By questions, comments or suggestions feel free to get in touch by creating an PR, issue or telepathically. 


### Cavecats [sic](https://www.youtube.com/watch?v=a0SuhNn8S60) 

Copyright 2019 Ivan Morozov, Gustav Behm, Tokn GmbH (https://upvest.co)

**google4s** is provided to you as free software under the MIT license.
The MIT software license is attached in the [COPYING](COPYING) file.

