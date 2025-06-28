package com.example.cats.effect

import cats.effect.IO

object Loop:

  val loop: IO[Unit] = IO.println("Hello, World!") >> loop
