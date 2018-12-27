package co.upvest.google4s.gstorage
import org.scalacheck.{Arbitrary, Gen}

trait Arbitrarys {

  private val legalChars = Gen.oneOf(Range('a', 'z').map(_.toChar))

  private val legalNameString = Gen
        .containerOf[Array, Char](legalChars)
        .suchThat(_.nonEmpty)
        .map(_.mkString)
        .suchThat(_.size > 3)

  implicit val genBName: Arbitrary[BucketName] =
    Arbitrary(legalNameString.map(BucketName))

  implicit val getBLName: Arbitrary[BlobName] =
    Arbitrary(legalNameString.map(BlobName))
}
