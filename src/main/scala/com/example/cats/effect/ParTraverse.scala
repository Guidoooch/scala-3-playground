package com.example.cats.effect

import cats.data.Chain
import cats.effect.{IO, IOApp}
import cats.effect.implicits.*

object ParTraverse extends IOApp.Simple:

  // This is your new "main"!
  def run: IO[Unit] =
    val start   = System.nanoTime()
    val numbers = Chain(Int.MinValue / 800 to Int.MaxValue / 800*)
    val end1    = System.nanoTime()
    val result  = numbers.parTraverseN(3)(i => IO(5f / i))
    val end2    = System.nanoTime()

    result.flatMap(squaredNumbers =>
      IO.println(
        s"""Squared numbers: ${squaredNumbers.length}
           |Time taken for construction: ${(end1 - start) / 1e9} seconds
           |Time taken for operation: ${(end2 - end1) / 1e9} seconds
           |""".stripMargin
      )
    )
