package com.example.fs2

import cats.effect.{IO, IOApp}
import fs2.Stream
import fs2.io.file.{Files, Path}

import scala.concurrent.duration.DurationInt

object Streams extends IOApp.Simple:

  private val fileStream = Files[IO]
    .readUtf8Lines(Path("origin.txt"))
    .evalTap(IO.println)
    .filterNot(_.isBlank)
    .compile
    .toList

  private val err4 =
    Stream(1, 2, 3).covary[IO] ++
    Stream.raiseError[IO](new Exception("bad things!")) ++
    Stream.eval(IO(4))

  private def repeat[F[_], O](stream: Stream[F, O]): Stream[F, O] =
    stream ++ repeat(stream)

  val run: IO[Unit] = for
    _           <- IO.println("FS2 examples...")
    lines       <- fileStream
    errorOrInts <- err4.compile.toList.attempt
    _           <- IO.println(s"Error or Ints: $errorOrInts")
    _           <- IO.println(lines)
    _           <- repeat(Stream(1, 2, 3).covary[IO]).take(10).compile.toList.flatMap(IO.println)
    _           <- Stream.repeatEval(IO(println("Hello, world!"))).metered(1.second).take(5).compile.drain
  yield ()
