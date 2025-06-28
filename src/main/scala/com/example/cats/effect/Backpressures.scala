package com.example.cats.effect

import cats.effect.std.Backpressure
import cats.effect.std.Backpressure.Strategy
import cats.effect.{IO, IOApp}
import cats.syntax.all.*

object Backpressures extends IOApp.Simple:

  val run: IO[Unit] = for
    _ <- IO.println("Backpressures example running...")
    b <- Backpressure[IO](Strategy.Lossless, 1)
    _ <- IO.println("Backpressure initialized with Lossy strategy.")
    f1 <- b.metered(IO.println("fiber 1 is running...")).start
    f2 <- b.metered(IO.println("fiber 2 is running...")).start
    _ <- (f1.join, f2.join).tupled
    _ <- IO.println("Backpressures example completed.")
  yield ()
