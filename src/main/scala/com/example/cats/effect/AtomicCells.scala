package com.example.cats.effect

import cats.data.Chain
import cats.effect.std.AtomicCell
import cats.effect.{IO, IOApp}
import cats.syntax.all.*

object AtomicCells extends IOApp.Simple:

  val run: IO[Unit] = for
    c <- AtomicCell[IO].of(0)
    _ <- IO.println("AtomicCells example running...")
    io <- Chain(0 until 256_000 *).parTraverse { _ =>
      c.getAndUpdate(_ + 1).flatMap(i => IO.println(s"Incremented to ${i + 1}") >> IO.pure(i)).start
    }
    _ <- io.parTraverse(f => f.joinWith(IO.println("Fiber cancelled, returning zero.") >> IO.pure(0)))
    _ <- c.get >>= (result => IO.println(s"AtomicCells example completed. Resulting value: $result"))
  yield ()
