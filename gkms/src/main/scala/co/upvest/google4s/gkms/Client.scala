package co.upvest.google4s.gkms

import co.upvest.google4s.core.{ HostPort, Llift, Project }
import co.upvest.terminology.adjectives.common.{ Encrypted, PlainText }
import com.google.api.gax.core.GoogleCredentialsProvider
import com.google.cloud.kms.v1.stub.{ KeyManagementServiceStubSettings => KMSSS }
import com.google.cloud.kms.v1.{ KeyManagementServiceClient, KeyManagementServiceSettings }
import com.google.protobuf.{ ByteString => Gbytes }

import scala.collection.JavaConverters._

trait Client[F[_]] {

  /**
   * Encrypts a plain text with the given Key. The encryption is
   * effectful in F.
   *
   * @param p Bytes to encrypt.
   * @param k GCP Key.
   * @return Encrypted bytes.
   */
  def encrypt(p: PlainText[Array[Byte]], k: Key): F[Encrypted[Array[Byte]]]

  /**
   * Decrypts a cypher text with the given Key. The decryption is
   * effectful in F.
   *
   * @param e Encrypted bytes.
   * @param k GCP Key.
   * @return Decrypted bytes.
   */
  def decrypt(e: Encrypted[Array[Byte]], k: Key): F[PlainText[Array[Byte]]]
}

object Client {

  final case class Config(
    gProjectName: Project,
    checkKeys: List[Key] = Nil,
    endpoint: HostPort = HostPort(KMSSS.getDefaultEndpoint)
  )

  private def buildKms(c: Config) = {

    val cred = GoogleCredentialsProvider
      .newBuilder()
      .setScopesToApply(KMSSS.getDefaultServiceScopes)
      .build

    val settings = KeyManagementServiceSettings
      .newBuilder()
      .setEndpoint(c.endpoint.s)
      .setCredentialsProvider(cred)
      .build()

    KeyManagementServiceClient.create(settings)
  }

  def apply[F[_]](c: Config)(lift: Llift[F]): Client[F] =
    new Client[F] {

      lazy val client = buildKms(c)

      override def encrypt(p: PlainText[Array[Byte]], k: Key) =
        lift { () =>
          val ep = client.encrypt(
            k.resourceId(c.gProjectName),
            Gbytes.copyFrom(p.t)
          ).getCiphertext.toByteArray
          Encrypted(ep)
        }

      override def decrypt(e: Encrypted[Array[Byte]], k: Key) =
        lift { () =>
            val pt = client.decrypt(
              k.resourceId(c.gProjectName),
              Gbytes.copyFrom(e.t)
            ).getPlaintext.toByteArray
          PlainText(pt)
        }

      private def checkKey(k: Key): Unit =
        k.usages map { us =>
          val expected = us map { _.permission } asJava
          val actual = client
            .testIamPermissions(
              k.resourceId(c.gProjectName),
              expected
            )
            .getPermissionsList

          require(expected.asScala.toSet == actual.asScala.toSet)
        } get
    }
}
