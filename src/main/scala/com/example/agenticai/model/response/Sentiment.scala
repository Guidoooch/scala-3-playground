package com.example.agenticai.model.response

import io.circe.Codec

case class Sentiment(magnitude: Double, score: Double)
  derives Codec.AsObject