package com.example.cats.effect

import cats.data.Chain
import cats.effect.{Deferred, IO, IOApp}
import cats.syntax.all.*

import scala.concurrent.duration.DurationInt

object Deferreds extends IOApp.Simple:

  val run: IO[Unit] = for
    d <- Deferred[IO, Int]
    f1 <- {
      IO.println("fast fiber before deferred") >>
      IO.sleep(3.seconds) >>
      d.complete(42) >>
      IO.println("fast fiber after deferred")
    }.start
    f2 <- Chain(0 until 256*).parTraverse { i =>
      IO.sleep(1.second) >>
      IO.println(s"slow fiber $i before deferred") >>
      d.get >>= (value => IO.println(s"slow fiber $i got value: $value"))
    }.start
    _ <- (f1.join, f2.join).tupled
  yield ()
