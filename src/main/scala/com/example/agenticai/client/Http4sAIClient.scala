package com.example.agenticai.client

import cats.effect.Async
import io.circe.*
import org.http4s.EntityEncoder
import org.http4s.Method.POST
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.client.Client
import org.http4s.{Header, Headers, Request, Uri}
import org.typelevel.ci.CIString

// --- Real AIClient using http4s ---
class Http4sAIClient[F[_]: Async](client: Client[F], apiUri: Uri) extends AIClient[F]:

  import Http4sAIClient.headers

  def annotateText[E: EntityEncoder[F, *], Response: Decoder](entity: E): F[Response] =
    val req =
      Request[F](method = POST, uri = apiUri)
        .withEntity[E](entity)
        .withHeaders(headers)

    client.expect[Response](req)

object Http4sAIClient:
  private val headers: Headers = Headers(Header.Raw(CIString("Content-Type"), "application/json"))
