package com.example.fs2

import cats.effect.{IO, IOApp}
import fs2.*

object AsyncEval extends IOApp.Simple {

  private val c = new Connection:
    def readBytes(onSuccess: Array[Byte] => Unit, onFailure: Throwable => Unit): Unit =
      Thread.sleep(200)
      onSuccess(Array(0, 1, 2))

  private val bytes = IO.async_[Array[Byte]] { cb => c.readBytesE(cb) }

  def run: IO[Unit] = for
    _ <- IO.println("Starting...")
    b <- Stream.eval(bytes).map(_.toList).compile.toVector
    _ <- IO.println(s"Read bytes: $b")
  yield println("Finished...")

  private trait Connection {
    def readBytes(onSuccess: Array[Byte] => Unit, onFailure: Throwable => Unit): Unit

    // or perhaps
    def readBytesE(onComplete: Either[Throwable, Array[Byte]] => Unit): Unit =
      readBytes(bs => onComplete(Right(bs)), e => onComplete(Left(e)))

    override def toString = "<connection>"
  }
}
