package com.example.cats.effect

import cats.effect.{IO, Resource}

import java.io.{File, FileInputStream, InputStream, OutputStream}

object FileCopier:

  private def inputOutputStreams(in: File, out: File): Resource[IO, (InputStream, OutputStream)] = for
    inStream <- Resource.fromAutoCloseable(IO(new FileInputStream(in)))
    outStream <- Resource.fromAutoCloseable(IO(new java.io.FileOutputStream(out)))
  yield (inStream, outStream)

  private def transfer(origin: InputStream, destination: OutputStream, buffer: Array[Byte], acc: Long): IO[Long] = for
    amount <- IO.blocking(origin.read(buffer, 0, buffer.length))
    count <-
      if (amount > -1)
        IO.blocking(destination.write(buffer, 0, amount)) >> transfer(origin, destination, buffer, acc + amount)
      else
        IO.pure(acc) // End of read stream reached (by java.io.InputStream contract), nothing to write
  yield count // Returns the actual amount of bytes transferred

  def copy(origin: File, destination: File): IO[Long] =
    inputOutputStreams(origin, destination).use { case (inStream, outStream) =>
      val buffer = new Array[Byte](8192) // 8KB buffer
      transfer(inStream, outStream, buffer, 0L)
    }
