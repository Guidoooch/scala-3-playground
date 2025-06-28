package com.example.cats.effect

import cats.effect.kernel.Ref
import cats.effect.{ExitCode, IO, IOApp, Sync}
import cats.syntax.all.*

object Refs extends IOApp:

  def run(args: List[String]): IO[ExitCode] = for {
    ref <- Ref.of[IO, Map[String, Int]](Map.empty)
    _   <- List("alice", "bob", "alice", "carol", "bob").parTraverse_(recordAccess(_, ref))
    _   <- ref.get.flatMap(map => IO.println(s"Final state: $map"))
  } yield ExitCode.Success

  private def recordAccess(user: String, ref: Ref[IO, Map[String, Int]]): IO[Unit] =
    ref.update { state =>
      state.updatedWith(user) {
        case Some(count) => Some(count + 1)
        case None        => Some(1)
      }
    }
