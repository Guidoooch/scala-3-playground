package com.example.http4s

import cats.effect.{IO, IOApp}
import com.comcast.ip4s.{ipv4, port}
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router

object Http4sServerApp extends IOApp.Simple:

  val run: IO[Unit] =
    val httpApp = Router(
      "/"     -> HelloWorldRestApi.routes,
      "/api"  -> TweetRestApi.routes,
      "/api"  -> StreamRestApi.routes
    ).orNotFound

    val serverAndShutdown =
      EmberServerBuilder
        .default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8080")
        .withHttpApp(httpApp)
        .build

    serverAndShutdown
      .use(server => IO.println(s"Server is running at ${server.address}") >> IO.never)
      .handleErrorWith { error => IO.println(s"Server failed with error: ${error.getMessage}") }
