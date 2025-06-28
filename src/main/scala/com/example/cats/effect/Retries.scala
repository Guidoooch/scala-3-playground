package com.example.cats.effect

import cats.data.{Kleisli, StateT}
import cats.effect.std.{Console, Random}
import cats.effect.{IO, IOApp, Sync}
import cats.implicits.*

import scala.concurrent.duration.*

object Retries extends IOApp.Simple:

  trait Delayable[F[_]]:
    extension [A](f: F[A]) def delayBy(duration: FiniteDuration): F[A]

  def retryWithPolicy[F[_]: {Console, Delayable, Sync}, A](fa: F[A], policy: RetryPolicy): F[A] =
    fa.handleErrorWith { error =>
      if policy.retries > 0 then
        Console[F].println(s"Retrying after ${policy.delay} due to error: ${error.getMessage}") >>
        retryWithPolicy(fa.delayBy(policy.delay), policy.next)
      else Sync[F].raiseError(error)
    }

  def retryWithStateT[F[_]: {Console, Delayable, Sync}, A](fa: F[A]): StateT[F, RetryPolicy, A] =
    StateT.get[F, RetryPolicy].flatMapF { currentState =>
      fa.handleErrorWith { error =>
        if currentState.retries > 0 then
          Console[F].println(s"Retrying after ${currentState.delay} due to error: ${error.getMessage}") >>
          retryWithStateT(fa.delayBy(currentState.delay)).runA(currentState.next)
        else
          Sync[F].raiseError(error)
      }
    }

  trait RetryPolicy(val delay: FiniteDuration, val retries: Int):
    def next: RetryPolicy

  class ExponentialBackoff(delay: FiniteDuration, retries: Int) extends RetryPolicy(delay, retries):

    def next: ExponentialBackoff = new ExponentialBackoff(delay * 2, retries - 1)

  val run: IO[Unit] =
    given Delayable[IO] with
      extension [A](f: IO[A]) def delayBy(duration: FiniteDuration): IO[A] = f.delayBy(duration)

    val randomBoolean: IO[Boolean] = Random.scalaUtilRandom[IO] >>= (_.nextIntBounded(10) map (_ < 3))

    val randomBooleanKleisli: Kleisli[IO, Any, Boolean] = Kleisli.liftF(randomBoolean)

    val programKleisli: Kleisli[IO, Boolean, Unit] =
      Kleisli[IO, Boolean, Unit](run =
        bool => if bool then IO.println("Hello, world!") else IO.raiseError(new RuntimeException("boom!"))
      )

    val kleisli: Kleisli[IO, Any, Unit] = randomBooleanKleisli andThen programKleisli

//    retryWithPolicy(io, new ExponentialBackoff(1.second, 2))
//      .handleErrorWith { error =>
//        IO.println(s"Failed with error: ${error.getMessage}")
//      }

    val io: IO[Unit] = kleisli.run("zubehor")

    retryWithStateT(io)
      .runA(new ExponentialBackoff(1.second, 2))
      .handleErrorWith { error =>
        IO.println(s"Failed with error: ${error.getMessage}")
      }
