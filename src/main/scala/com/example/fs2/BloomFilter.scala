package com.example.fs2

import cats.effect.std.Random
import cats.effect.{IO, IOApp, Ref}
import fs2.io.file.{Files, Path}
import org.typelevel.log4cats.slf4j.Slf4jFactory
import org.typelevel.log4cats.{LoggerFactory, SelfAwareStructuredLogger}

import scala.annotation.tailrec
import scala.util.hashing.{ByteswapHashing, MurmurHash3}

object BloomFilter extends IOApp.Simple:

  override def run: IO[Unit] = for
    bitsRef <- Ref[IO].of(Array.fill(size)(false))
    _ <-
      fileStream("words.txt")
        .mapAsync(4): word =>
          val (index1, index2) = indices(word)

          bitsRef.update: bits =>
            bits.update(index1, true)
            bits.update(index2, true)
            bits
        .compile
        .drain
    counter <- Ref[IO].of(0L)
    _ <-
      fs2.Stream
        .repeatEval(nextPrintableChar)
        .chunkN(5, allowFewer = false)
        .evalMapChunk(chars => IO.pure(chars.toArray.mkString))
        .take(479_912_559)
        .mapAsync(4): string =>
          counter
            .updateAndGet(_ + 1)
            .flatMap: count =>
              bitsRef.get.flatMap: bits =>
                val (index1, index2) = indices(string)
                if bits(index1) && bits(index2) then
                  fileStream("words.txt")
                    .takeThrough(_ == string)
                    .compile
                    .last
                    .flatMap:
                      case Some(found) =>
                        logger.info(s"Bloom filter worked well for word #$count: $found")
                      case _ =>
                        logger.info(s"False positive in Bloom Filter for word #$count: $string")
                else
                  logger.trace(s"Word #$count not present in vocabulary: $string")
        .compile
        .drain
  yield ()

  private val logger: SelfAwareStructuredLogger[IO] =
    given LoggerFactory[IO] = Slf4jFactory.create[IO]
    LoggerFactory[IO].getLogger

  private def fileStream(string: String): fs2.Stream[IO, String] =
    Files.forIO.readUtf8Lines(Path(getClass.getClassLoader.getResource(string).getPath))

  private val size = 479_912_559

  private def indices(word: String): (Int, Int) =
    val bytesSwap = math.floorMod(ByteswapHashing[String].hash(word), size)
    val murmur    = math.floorMod(MurmurHash3.stringHash(word), size)
    (bytesSwap, murmur)

  private def nextPrintableChar: IO[Char] =
    Random.scalaUtilRandom[IO].flatMap:
      _.nextPrintableChar.flatMap: char =>
        if char.isLetter then IO.pure(char)
        else nextPrintableChar
