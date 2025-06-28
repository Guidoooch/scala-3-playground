package com.example.cats.effect

import cats.effect.*
import cats.effect.std.CyclicBarrier
import cats.implicits.*

import scala.concurrent.duration.DurationInt

object CyclicBarriers extends IOApp.Simple:

  val run: IO[Unit] = for
    b <- CyclicBarrier[IO](2)
    f1 <- {
      IO.println("fast fiber before barrier") >>
      b.await >>
      IO.println("fast fiber after barrier")
    }.start
    f2 <- {
      IO.sleep(1.second) >>
      IO.println("slow fiber before barrier") >>
      IO.sleep(1.second) >>
      b.await >>
      IO.println("slow fiber after barrier")
    }.start
    _ <- (f1.join, f2.join).tupled
  yield ()
