package co.upvest.google4s
import co.upvest.google4s.core.Project

/*
 * Cloud KMS is a cloud-hosted key management service that lets you manage cryptographic keys for your
 * cloud services the same way you do on premises. You can generate, use, rotate,
 * and destroy AES256, RSA 2048, RSA 3072, RSA 4096, EC P256, and EC P384 cryptographic keys.
 * https://cloud.google.com/kms/
 */
package object gkms {

  /**
   * A key is a named object representing a cryptographic key used for a specific purpose.
   */
  final case class Key(
    name: Key.Name,
    ring: Key.Ring,
    location: Key.Location = Key.Location.Global,
    usages: Option[List[Key.Usage]] = None
  ) {

    /**
     * @param p GCP Project Name with the Key.
     * @return Fully qualified path to the particular Key in google cloud.
     * @see <a href=https://cloud.google.com/kms/docs/object-hierarchy>object-hierarchy</a>
     */
    def resourceId(p: Project) = String.format(
      "projects/%s/locations/%s/keyRings/%s/cryptoKeys/%s",
      p.s,
      location.s,
      ring.s,
      name.s
    )
  }

  object Key {

    /**
     * The name of the key, under "cryptoKeys" in resource ID.
     */
    final case class Name(s: String) extends AnyVal

    /**
     * A key ring is a grouping of keys for organizational purposes.
     * A key ring belongs to a GCP Project and resides in a specific location.
     * Keys inherit permissions from the key ring that contains them. Grouping
     * keys with related permissions together in a key ring allows you to grant,
     * revoke, or modify permissions to those keys at the key ring level,
     * without needing to act on each one individually.
     * @see <a href=https://cloud.google.com/kms/docs/object-hierarchy#key_ring>KeyRing</a>
     */
    final case class Ring(s: String) extends AnyVal

    /**
     * Within a project, Cloud KMS resources can be created in multiple locations.
     * These represent the geographical data center location where requests to Cloud KMS
     * regarding a given resource are handled, and where the corresponding
     * cryptographic keys are stored.
     * @see <a href=https://cloud.google.com/kms/docs/object-hierarchy#location>Location</a>
     */
    final case class Location(s: String) extends AnyVal

    object Location {
      /*
        There is a special location for Cloud KMS resources called global.
        When created in the global location, your Cloud KMS resources are available
        from multiple data centers.
        @see <a href=https://cloud.google.com/kms/docs/locations>Locations</a>
       */
      val Global = Location("global")
    }

    /**
     * Usage describes the Gooogle Cloud IAM Roles, used in the
     * particular key.
     * @see <a href=https://cloud.google.com/kms/docs/iam>IAM</a>
     */
    sealed trait Usage {

      /**
       * IAM Permissions controlling what the Key can be used for in context of GCP.
       * @see <a href=https://cloud.google.com/kms/docs/reference/permissions-and-roles>Permissions and Roles</a>
       */
      def permission: String
    }

    object Usage {

      case object Encrypt extends Usage {
        val permission = "cloudkms.cryptoKeyVersions.useToEncrypt"
      }

      case object Decrypt extends Usage {
        val permission = "cloudkms.cryptoKeyVersions.useToDecrypt"
      }
    }
  }
}
