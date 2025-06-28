package com.example.http4s

import cats.effect.IO
import org.http4s.HttpRoutes
import org.http4s.dsl.io.*
import scala.concurrent.duration.DurationInt

object StreamRestApi:

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "stream" / name =>
      Ok(
        fs2.Stream
          .emits(Seq("Hello", name, "from", "http4s"))
          .covary[IO]
          .intersperse(" ")
          .metered(1.second)
      )
  }
