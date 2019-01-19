# Google4s

[![Build Status](https://circleci.com/gh/toknapp/google4s.svg?style=svg)](https://circleci.com/gh/toknapp/google4s)
[![Latest version](https://index.scala-lang.org/toknapp/google4s/google4s-gkms/latest.svg?color=orange)](https://index.scala-lang.org/toknapp/google4s/google4s-gkms)
[![Latest version](https://index.scala-lang.org/toknapp/google4s/google4s-gpubsub/latest.svg?color=orange)](https://index.scala-lang.org/toknapp/google4s/google4s-gpubsub)
[![Latest version](https://index.scala-lang.org/toknapp/google4s/google4s-gstorage/latest.svg?color=orange)](https://index.scala-lang.org/toknapp/google4s/google4s-gstorage)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](COPYING)


This project aims to provide safe, composable and easy to use clients for [Google Cloud Platform Services](https://github.com/googleapis/google-cloud-java) written
in functional style **Scala**.

Currently supported clients are

- Google Cloud Storage
- Google Cloud PubSub
- Google Cloud Key Management Service (KMS)


The client implementations are using Google's Java clients underneath
however, the Java code is mostly generated so it's not really fun to use it.

 - Omniversal nested builder pattern
 - No or limited concurrency
 - Uncomposable patterns 

The clients are structured and released as separate modules.
Add the latest version to your project

### Latest Stable
```sbt
libraryDependencies += "co.upvest.google4s" %% "google4s-..."  % "..."
```
### Latest Snapshot
```sbt
libraryDependencies += "co.upvest.google4s" %% "google4s-..."  % "0..-SNAPSHOT"
```

## Examples/Doc
Please refer to the [documentation page](https://toknapp.github.io/google4s/):


## Contributing

Coming soon... PR's are welcome and highly appreciated!

## Limitations

- We try to implement the configs as lean as possible. As the result some advanced
configuration could be missing. If you encounter this problem **please create an issue or a PR**
we are happy to add what is missing.

- Please refer to the specific [documentation page](https://toknapp.github.io/google4s/):
 
## Contact

By questions, comments or suggestions feel free to get in touch by creating an PR, issue or telepathically. 

### Cavecats [sic](https://www.youtube.com/watch?v=a0SuhNn8S60) 

Copyright 2019 Ivan Morozov, Gustav Behm, Tokn GmbH (https://upvest.co)

**google4s** is provided to you as free software under the MIT license.
The MIT software license is attached in the [COPYING](COPYING) file.
