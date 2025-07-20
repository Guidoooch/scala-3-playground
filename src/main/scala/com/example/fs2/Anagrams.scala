package com.example.fs2

import cats.effect.{IO, IOApp, Ref}
import fs2.io.file.{Files, Path}

object Anagrams extends IOApp.Simple:

  override def run: IO[Unit] = for
    anagramsRef <- Ref[IO].of(Map.empty[String, Seq[String]])
    result <-
      fileStream("words.txt")
        .map: current =>
          current -> current.permutations.toSeq
        .map:
          case (current, permutations) =>
            anagramsRef
              .getAndUpdate: anagrams =>
                val present =
                  permutations.flatMap: p =>
                    val anagram = anagrams.get(p)
                    anagram match
                      case Some(value) => Some(p -> value)
                      case None => None
                  .headOption

                val newValue = present match
                  case Some((word, values)) => word -> (current +: values)
                  case None => current -> Seq.empty

                anagrams + newValue

        .compile
        .last

    _ <- IO.println(result)
  yield ()

  private def fileStream(string: String): fs2.Stream[IO, String] =
    Files.forIO.readUtf8Lines(Path(getClass.getClassLoader.getResource(string).getPath))
