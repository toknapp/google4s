package co.upvest.google4s.gpubsub

import org.scalacheck.Gen

trait Arbitrarys {

    val nonEmptyBytePayloads = Gen
        .nonEmptyListOf(Gen.alphaUpperChar.map(_.toByte))

    val nonEmptyPayloadSeqs: Gen[List[Array[Byte]]] =
      Gen.nonEmptyListOf(nonEmptyBytePayloads.map(_.toArray))
}
