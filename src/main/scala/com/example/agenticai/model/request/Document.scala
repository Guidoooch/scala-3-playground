package com.example.agenticai.model.request

import io.circe.*

case class Document(
    `type`: String, // e.g. "PLAIN_TEXT", "HTML"
    content: String,
    languageCode: Option[String] = None // optional, e.g. "en"
) derives Codec.AsObject
