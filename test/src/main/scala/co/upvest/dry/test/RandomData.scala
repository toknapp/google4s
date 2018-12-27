package co.upvest.dry.test

import co.upvest.dry.essentials._

import scala.util.Random

trait RandomData {
  def randomPositiveBigInt(upperBound: Long, lowerBound: Long): BigInt =
    BigInt(randomPositiveLong(upperBound, lowerBound))

  def randomPositiveLong(upperBound: Long, lowerBound: Long): Long =
    (Random.nextLong().abs % (upperBound - lowerBound)) + lowerBound

  def randomBytes(n: Int): Bytes = {
    val bs = new Bytes(n)
    Random.nextBytes(bs)
    bs
  }

  def salt: String             = salt()
  def salt(n: Int = 5): String = Random.alphanumeric.take(5).mkString
}

object RandomData extends RandomData
