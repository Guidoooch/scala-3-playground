package com.example.cats.core

import cats.effect.{IO, IOApp}
import io.circe.Codec
import org.http4s.circe.jsonOf
import org.http4s.EntityDecoder

object Kleislis extends IOApp.Simple:

  case class User(name: String, email: String) derives Codec.AsObject

  given userEntityDecoder: EntityDecoder[IO, User] = jsonOf[IO, User]

  val run: IO[Unit] =
    IO.println("Kleisli example...") >>
    IO.println("Finished!")
