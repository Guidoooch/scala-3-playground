package com.example.agenticai.model.response

import io.circe.Codec

case class Entity(
    name: String,
    `type`: String,
    metadata: Map[String, String],
    mentions: List[Mention]
) derives Codec.AsObject