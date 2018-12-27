package co.upvest.google4s.gstorage

import java.time.Instant

import cats.Monad
import com.google.cloud.{ storage => G }
import cats.implicits._
import co.upvest.google4s.core._
import scala.util.Try
import scala.collection.JavaConverters._

trait Client[F[_]] {

  /**
   * Fetches a Blob if exists to the corresponding identifier.
   * The effect of searching and fetching the data of the Blob
   * is been computed in F.
   */
  def get(id: Blob.Id): F[Option[Blob[F]]]

  /**
   * Lists blobs in buckets by using a potential filter condition as a prefix.
   * Prefixes are separated by '/' in the "bucket - blob" world.
   */
  def list(bucket: BucketName, blobPrefix: Option[BlobName]): F[List[Blob.Id]]

  /**
   * Creates or Updates the data to the corresponding Blob id.
   */
  def put(id: Blob.Id, data: Array[Byte], contentType: ContentType = DefaultContentType): F[Blob[F]]
}

object Client {
  implicit class Ops[F[_]](client: Client[F]) {

    /**
     * Finds the last version to a bucketName under the potential prefix.
     * Latest is determined by the custom ordering function see
     * {@see co.upvest.google4s.gstorage.Blob.class#ordering[F[_]}
     *
     */
    def latest(
      bucket: BucketName,
      prefix: Option[BlobName]
    )(implicit M: Monad[F]): F[Option[Blob[F]]] = (
      client.list(bucket, prefix)
        map { _ map client.get }
        flatMap { _.toList.sequence }
        map { _.flatten } map {
        case Nil      => None
        case nonEmpty => Some(nonEmpty max Blob.ordering[F])
      }
    )
  }

  final case class Config(gProject: Project)

  def apply[F[_]](client: => G.Storage)(lift: Llift[F]): Client[F] =
    new Client[F] {

      override def get(id: Blob.Id) = lift { () =>
        Option(client.get(id.gBlobId)) map mkBlob
      }

      override def put(id: Blob.Id, data: Array[Byte], contentType: ContentType) =
        lift { () =>
          val bi = G.BlobInfo
            .newBuilder(id.gBlobId)
            .setContentType(contentType.s)
            .build()
          mkBlob(client.create(bi, data))
        }

      private def mkBlob(gb: G.Blob): Blob[F] = {
        val id = Blob.Id.of(gb)
        val ct = gb.getContentType()
        Blob(
          id,
          data = () =>
            lift { () =>
              gb.getContent()
            },
          update = { bs =>
            this.put(id, bs, ContentType(ct))
          },
          createTime = interpretGStorageTime(gb.getCreateTime()),
          updateTime = interpretGStorageTime(gb.getUpdateTime()),
          ContentType(gb.getContentType())
        )
      }

      //in RFC 3339 format.
      private def interpretGStorageTime(l: Long) = l match {
        case 0  => None
        case nz => Try { Instant ofEpochMilli nz } toOption
      }

      override def list(bucket: BucketName, blobPrefix: Option[BlobName]) =
        lift { () =>
          val opts = blobPrefix.toSeq.map { bn =>
            G.Storage.BlobListOption.prefix(bn.s)
          }
          client
            .list(bucket.s, opts: _*)
            .iterateAll
            .iterator
            .asScala
            .map(Blob.Id.of)
            .to[List]
        }
    }

  def apply[F[_]](config: Config)(lift: Llift[F]): Client[F] =
    apply[F](buildClient(config))(lift)

  private def buildClient(c: Config) =
    G.StorageOptions
      .newBuilder()
      .setProjectId(c.gProject.s)
      .build()
      .getService
}
