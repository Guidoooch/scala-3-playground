package com.example.http4s

import cats.effect.IO
import org.http4s.HttpRoutes
import org.http4s.dsl.io.*

object HelloWorldRestApi:
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "hello" / name =>
      Ok(s"Hello, $name.")
  }
