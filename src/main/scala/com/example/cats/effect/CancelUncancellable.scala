package com.example.cats.effect

import cats.effect.std.Semaphore
import cats.effect.syntax.all.*
import cats.effect.{IO, IOApp, MonadCancel}
import cats.syntax.all.*

import scala.concurrent.duration.DurationInt

object CancelUncancellable extends IOApp.Simple:

  override def run: IO[Unit] =
    val semaphoreIO = Semaphore[IO](2)
    val alloc       = IO.pure("Resource")
    val use         = (r: String) => IO.println(s"Using resource: $r").delayBy(5.seconds)
    val release     = (r: String) => IO.println(s"Releasing resource: $r")

    for
      s  <- semaphoreIO
      _  <- IO.println("Starting guarded operation...")
      f1 <- guarded(s, alloc)(use)(release).start
      f2 <- guarded(s, alloc)(use)(release).start
      _  <- f1.cancel
      _  <- f1.joinWith(IO.println("First operation cancelled!"))
      _  <- f2.joinWith(IO.println("Second operation cancelled!"))
      _  <- IO.println(s"Operation completed successfully.")
    yield ()

  private def guarded[F[_], R, A, E]
    (s: Semaphore[F], alloc: F[R])
    (use: R => F[A])
    (release: R => F[Unit])
    (using F: MonadCancel[F, E])
  : F[A] =
    F uncancelable { poll =>
      for {
        r <- alloc

        _ <- poll(s.acquire).onCancel(release(r))
        releaseAll = s.release >> release(r)

        a <- poll(use(r)).guarantee(releaseAll)
      } yield a
    }
