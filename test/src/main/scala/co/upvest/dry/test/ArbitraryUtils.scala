package co.upvest.dry.test

import org.scalacheck.Arbitrary

trait ArbitraryUtils {
  def pick[T](implicit T: Arbitrary[T]): T =
    T.arbitrary.sample match {
      case Some(t) => t
      case None    => pick[T]
    }
}

object ArbitraryUtils extends ArbitraryUtils
