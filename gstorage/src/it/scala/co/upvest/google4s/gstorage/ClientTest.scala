package co.upvest.google4s.gstorage
import java.util.concurrent.atomic.AtomicInteger

import cats.arrow.FunctionK
import cats.implicits._
import cats.{Id, Monad, ~>}
import com.google.cloud.storage.contrib.nio.testing.LocalStorageHelper
import org.scalatest._
import org.scalatest.prop.GeneratorDrivenPropertyChecks

class ClientTest extends WordSpec with Matchers with GeneratorDrivenPropertyChecks with Arbitrarys {

  val storage         = LocalStorageHelper.getOptions.getService
  val idRun: Id ~> Id = FunctionK.id

  def storageTests[F[_]: Monad](client: Client[F], run: F ~> Id): Unit =
    "Gstorage client" should {
      "create a bucket and a blob" in {
        forAll { (b: BucketName, bn: BlobName, payload: Array[Byte]) =>
          val id = Blob.Id(b, bn, None)

          run { client.put(id, payload) map { _.data() } } should matchPattern {
            case b: Array[Byte] if (b.deep == payload.deep) =>
          }
        }
      }

      "update blob in existing bucket" in {
        forAll { (b: BucketName, bn: BlobName, payload: Array[Byte], payloadNew: Array[Byte]) =>
          val id   = Blob.Id(b, bn, None)
          val blob = run { client.put(id, payload) }

          run { client.put(id, payloadNew) }

          val `blob'data` = run { client.get(id) map { _.map(_.data()) } }

          `blob'data` should be('defined)
          `blob'data` should matchPattern {
            case Some(data: Array[Byte]) if (data.deep == payloadNew.deep) =>
          }

          run { blob.update(payload) }

          val `blob''data` = run { client.get(id) map { _.map(_.data()) } }

          `blob''data` should matchPattern {
            case Some(data: Array[Byte]) if (data.deep == payload.deep) =>
          }
        }
      }

      "list all blobs by prefix" in {
        val counter  = new AtomicInteger(0)
        val prefix   = "prefix/"
        val bucketYo = BucketName("bucketyo")

        forAll { (bn: BlobName, payload: Array[Byte]) =>
          val blob = BlobName(s"$prefix${bn.s}")
          val id   = Blob.Id(bucketYo, blob, None)
          run { client.put(id, payload) }
          counter.incrementAndGet()
        }

        run {
          client.list(bucketYo, Some(BlobName(prefix)))
        }.size shouldBe counter.get().toInt
      }

    }

  "Gstorage client tests" should {
    "be able to use Id" should {
      import co.upvest.google4s.core.IdLift._
      storageTests[Id](Client(storage)(liftId), idRun)
    }
  }

}
