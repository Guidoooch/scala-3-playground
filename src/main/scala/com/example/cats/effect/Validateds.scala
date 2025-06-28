package com.example.cats.effect

import cats.data.Validated.{Invalid, Valid}
import cats.data.{Chain, Validated}
import cats.effect.std.Random
import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.all.*

object Validateds extends IOApp:

  override def run(args: List[String]): IO[ExitCode] = for
    // Your application logic here
    _ <- IO(println("Hello, Validated!"))
    strings <-
      Chain(randomStringT, randomStringT, randomStringT, randomStringT).map(_.map(evenStringValidator)).sequence

    result = strings match
      case Chain(v1, v2, v3, v4) =>
        (v1, v2, v3, v4).mapN { case (v1, v2, v3, v4) => v1 && v2 && v3 && v4 }
      case _ =>
        Invalid("Nozzino' stri'")

    _ <- result match
      case Valid(_) =>
        IO(println("All strings are even!"))
      case Invalid(errors) =>
        IO(println(s"Errors found: $errors"))

    _ <- IO(println("Bye bye, Validated!"))
  yield ExitCode.Success

  private val randomStringT: IO[String] = Random[IO].nextIntBounded(4) >>= (length => Random[IO].nextString(length))

  private def evenStringValidator(s: String): Validated[String, Boolean] =
    if s.length % 2 == 0 then
      Valid(true)
    else
      Invalid(s"$s length is not even ")
