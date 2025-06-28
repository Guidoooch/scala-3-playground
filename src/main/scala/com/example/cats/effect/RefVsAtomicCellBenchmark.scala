package com.example.cats.effect

import cats.data.Chain
import cats.effect.*
import cats.effect.std.AtomicCell
import cats.syntax.all.*
import cats.{Applicative, Parallel}

import java.util.concurrent.TimeUnit

object RefVsAtomicCellBenchmark extends IOApp:

  private val fiberCount      = 999
  private val updatesPerFiber = 999

  def run(args: List[String]): IO[ExitCode] = for {
    _ <- IO.println(s"Running with $fiberCount fibers × $updatesPerFiber updates")
    _ <- benchmarkAtomicCell
//    _ <- benchmarkRef
  } yield ExitCode.Success

  private def benchmarkAtomicCell: IO[Unit] = for
    cell   <- AtomicCell[IO].of(0)
    start  <- IO.monotonic
    _      <- parallelComputation(fiberCount, cell)
    end    <- IO.monotonic
    result <- cell.get
    _      <- IO.println(s"[Atomic] Final count: $result in ${(end - start).toUnit(TimeUnit.SECONDS)} s")
  yield ()

  private def benchmarkRef: IO[Unit] = for
    ref    <- Ref[IO].of(0)
    start  <- IO.monotonic
    _      <- parallelComputation(fiberCount, ref)
    end    <- IO.monotonic
    result <- ref.get
    _      <- IO.println(s"[Ref]    Final count: $result in ${(end - start).toUnit(TimeUnit.SECONDS)} s")
  yield ()

  private def parallelComputation[F[_]: {Applicative, Parallel}](
      fiberCount: Int,
      primitive: AtomicCell[F, Int] | Ref[F, Int]
  ) =
    val function = primitive match {
      case atomicCell: AtomicCell[F, Int] =>
//        atomicCell.get
        atomicCell.update(_ + 1)

      case ref: Ref[F, Int] =>
//        ref.get
        ref.update(_ + 1)
    }

    Chain(0 until fiberCount*).parTraverse_ { _ =>
      Chain(0 until updatesPerFiber*).traverse_ { _ =>
        function
      }
    }
