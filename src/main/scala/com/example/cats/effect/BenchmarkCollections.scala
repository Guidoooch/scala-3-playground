package com.example.cats.effect

import cats.data.Chain
import cats.effect.{IO, IOApp}

object BenchmarkCollections extends IOApp.Simple:

  // This is your new "main"!
  def run: IO[Unit] =
    val Max = Int.MaxValue / 10000

    val addChain =
      IO(benchmarkChain("", _ + _)(Chain[String](Seq.tabulate(Max)(_.toString)*))).flatMap(printResult.tupled)

    IO.println("Benchmarking collection addition...") >>
//      IO(benchmarkFunction(Vector.tabulate(Max)(_.toString))).flatMap(printResult.tupled)
    addChain

  private def benchmarkFunction[T](iterable: => Iterable[T]): (Double, Double) =
    val start  = System.nanoTime()
    val values = iterable
    val end1   = System.nanoTime()
    values.foldLeft("")(_ + _): Unit
    val end2  = System.nanoTime()
    val time1 = (end1 - start) / 1e9
    val time2 = (end2 - end1) / 1e9
    (time1, time2)

  private def benchmarkChain[T](unit: T, function: (T, T) => T)(chain: => Chain[T]): (Double, Double) =
    val start  = System.nanoTime()
    val values = chain
    val end1   = System.nanoTime()
    values.foldLeft(unit)(function): Unit
    val end2  = System.nanoTime()
    val time1 = (end1 - start) / 1e9
    val time2 = (end2 - end1) / 1e9
    (time1, time2)

  private def printResult(time1: Double, time2: Double): IO[Unit] =
    IO.println(s"Construction time: $time1 seconds") >>
    IO.println(s"Addition time: $time2 seconds")
