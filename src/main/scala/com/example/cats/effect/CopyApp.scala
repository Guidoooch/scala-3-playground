package com.example.cats.effect

import cats.effect.{ExitCode, IO, IOApp}
import com.example.cats.effect.{FileCopier, HelloWorld}

import java.io.File

object CopyApp extends IOApp:

  // This is your new "main"!
  def run(args: List[String]): IO[ExitCode] =
    for {
      _ <- HelloWorld.say().flatMap(IO.println)
      //      _ <- Loop.loop.timeout(2.seconds).handleErrorWith(_ => IO.println("Loop timed out!"))
      _ <- IO.raiseWhen(args.length < 2)(new IllegalArgumentException("Need origin and destination files"))
      orig = new File(args.head)
      dest = new File(args.tail.head)
      _ <- IO.whenA(!orig.exists()) {
        // Cancel the program if the origin file does not exist
        IO.println(s"Origin file ${orig.getPath} does not exist, exiting...") >>
        IO.canceled
      }
      _ <-
        if dest.exists() then
          IO.println("Destination file already exists, do you want to overwrite it? (y/n)") >>
          IO.readLine.flatMap { answer =>
            if answer.head.toLower == 'y' then
              IO.println(s"Overwriting ${dest.getPath}...") // This is just a message, no actual overwrite yet
            else
              // Cancel the program if the user does not want to overwrite
              IO.println(s"Not overwriting ${dest.getPath}, exiting...") >>
              IO.canceled
          }
        else IO.unit

      count <- FileCopier.copy(orig, dest)
      _     <- IO.println(s"$count bytes copied from ${orig.getPath} to ${dest.getPath}")
    } yield ExitCode.Success
