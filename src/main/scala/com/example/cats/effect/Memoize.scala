package com.example.cats.effect

import cats.data.Chain
import cats.effect.{IO, IOApp}
import cats.syntax.all.*

object Memoize extends IOApp.Simple:

  def run: IO[Unit] =
    val action: IO[String] = IO.println("This is only printed once").as("action")

    val x: IO[String] = for {
      memoized <- action.memoize
      results  <- Chain(0 to Int.MaxValue / 1000*).parTraverse(_ => memoized)
    } yield results.fold

    x.flatMap(IO.println)
