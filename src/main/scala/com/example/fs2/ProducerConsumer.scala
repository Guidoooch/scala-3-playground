package com.example.fs2

import cats.effect.{IO, IOApp}
import cats.effect.std.Queue
import fs2.*

import scala.concurrent.duration.*

object ProducerConsumer extends IOApp.Simple:

  private def producer(queue: Queue[IO, Int]): Stream[IO, Chunk[Unit]] =
    Stream
      .range(0, 100)
      .covary[IO]
      .metered(500.millis)
      .chunkN(10) // <- backpressure if >10
//      .evalMap(i => IO(println(s"Producing $i")) >> queue.offer(i))
      .evalMapChunk(_.traverse(i => IO(println(s"Producing $i")) >> queue.offer(i)))
//      .metered(100.millis)

  private def consumer(queue: Queue[IO, Int]): Stream[IO, Unit] =
    Stream
      .repeatEval(queue.tryTake)
      .metered(500.millis)
      .evalMap {
        case None    => IO.unit
        case Some(i) => IO(println(s"Consuming $i"))
      }

  val run: IO[Unit] = for
    queue <- Queue.bounded[IO, Int](100) // <- backpressure if >10
    _     <- (producer(queue) concurrently consumer(queue)).compile.drain
  yield ()
