package co.upvest.google4s
import java.time.Instant
import com.google.cloud.{ storage => G }

/**
 *  Cloud Storage allows world-wide storage and retrieval of any amount of data at any time.
 *  You can use Cloud Storage for a range of scenarios including serving website content,
 *  storing data for archival and disaster recovery, or distributing large data objects
 *  to users via direct download.
 *  https://cloud.google.com/storage/docs/
 */
package object gstorage {

  /**
   * Blob names have to follow constraints
   * @see <a href=https://cloud.google.com/storage/docs/naming#objectnames>Blob name requirements</a>
   */
  final case class BlobName(s: String) extends AnyVal

  /**
   * Bucket names have to follow constraints
   * @see <a href=https://cloud.google.com/storage/docs/naming#bucketnames>Bucket name requirements</a>
   */
  final case class BucketName(s: String) extends AnyVal

  final case class ContentType(s: String) extends AnyVal

  final case class Generation(l: java.lang.Long) extends AnyVal

  val DefaultContentType = ContentType("application/octet-stream")

  /**
   * A wrapper around the Google Cloud Storage concept of an Object.
   *
   * @param id A custom unique identifier for a Blob.
   * @param data Lazy evaluated call for fetching the bytes corresponding to the Id. Effectful in F.
   * @param update A convinience hook, calling `put` on the Client to update the blob.
   * @param createTime The creation time of the object. Converted from RFC 3339.
   * @param updateTime The modification time of the object metadata. Converted from RFC 3339.
   * @param contentType ContentType of the payload. Default is  `application/octet-stream`.
   * @tparam F effect type.
   */
  final case class Blob[F[_]] private (
    id: Blob.Id,
    data: () => F[Array[Byte]],
    update: Array[Byte] => F[Blob[F]],
    createTime: Option[Instant],
    updateTime: Option[Instant],
    contentType: ContentType
  )

  object Blob {
    object Id {

      /**
       * Composes BucketName, BlobName and DataGeneration Timestamp
       * to create a unique Id of the Data inside the Blob.
       */
      def of(gb: G.BlobInfo) = Id(
        BucketName(gb.getBucket),
        BlobName(gb.getName),
        Option(gb.getGeneration) map Generation
      )
    }

    final case class Id(bucket: BucketName, blob: BlobName, generation: Option[Generation]) {
      // generation == None means latest version
      lazy val gBlobId = generation match {
        case Some(t) => G.BlobId.of(bucket.s, blob.s, t.l)
        case None    => G.BlobId.of(bucket.s, blob.s)
      }
    }

    implicit def ordering[F[_]] = Ordering.by { b: Blob[F] =>
      b.updateTime orElse b.createTime
    }
  }
}
