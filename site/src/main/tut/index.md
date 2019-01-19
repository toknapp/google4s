---
layout: home
position: 1
section: home
title: "Home"
---

# Google4s

This project aims to provide safe, composable and easy to use clients for [Google Cloud Platform Services](https://github.com/googleapis/google-cloud-java) written
in functional style **Scala**.

### Currently supported clients are

- [Google Cloud Storage](./gstorage/index.html)

- [Google Cloud PubSub](./gpubsub/index.html)

- [Google Cloud Key Management Service (KMS)](./gkms/index.html)


The client implementations are using Google's Java clients underneath
however, the Java code is mostly generated so it's not really fun to use it.
Google4s abstracts from:

 - **Omniversal nested builder pattern**
 - **No or limited concurrency**
 - **Uncomposable patterns**
 
Additionally to the original cloud storage implementation, this project utilizes:
 - [Cats](https://github.com/typelevel/cats) for FP fuzz..
 - [Kind Projector](https://github.com/non/kind-projector) because we can.
 - [Terminology](https://github.com/toknapp/terminology) for tagging primitives.
 
## Contact

By questions, comments or suggestions feel free to get in touch by creating a PR, issue or telepathically. 


### Cavecats [sic](https://www.youtube.com/watch?v=a0SuhNn8S60) 

Copyright 2019 Ivan Morozov, Gustav Behm, Tokn GmbH (https://upvest.co)

**google4s-gstorage** is provided to you as free software under the MIT license.
The MIT software license is attached in the [COPYING](./../../../../COPYING) file. 