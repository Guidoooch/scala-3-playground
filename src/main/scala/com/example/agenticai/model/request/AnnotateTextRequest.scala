package com.example.agenticai.model.request

import com.example.agenticai.model.request.{Document, Features}
import io.circe.{Codec, Encoder}

case class AnnotateTextRequest(
    document: Document,
    features: Features,
    encodingType: Option[String] = None // e.g. "UTF8", "UTF16", "UTF32"
) derives Codec.AsObject
