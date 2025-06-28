package com.example.agenticai.model.response

import io.circe.Codec

case class Category(
    name: String,
    confidence: Double,
    severity: Int
) derives Codec.AsObject