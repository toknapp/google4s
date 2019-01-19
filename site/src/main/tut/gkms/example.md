---
layout: docs
title:  "Example"
sections: gkms
---

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