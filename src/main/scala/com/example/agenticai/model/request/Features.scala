package com.example.agenticai.model.request

import io.circe.Codec

case class Features(
    extractEntities: Boolean,
    extractDocumentSentiment: Boolean,
    classifyText: Boolean,
    moderateText: Boolean
) derives Codec.AsObject

object Features:
  val All: Features = Features(
    extractEntities = true,
    extractDocumentSentiment = true,
    classifyText = true,
    moderateText = true
  )
