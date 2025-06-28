package com.example.agenticai.client

import io.circe.Decoder
import org.http4s.EntityEncoder

// --- Define AIClient trait as before ---
trait AIClient[F[_]]:
  def annotateText[E: EntityEncoder[F, *], Response: Decoder](entity: E): F[Response]
