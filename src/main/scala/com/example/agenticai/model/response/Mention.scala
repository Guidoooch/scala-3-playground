package com.example.agenticai.model.response

import io.circe.Codec

case class Mention(text: TextInfo, `type`: String, probability: Double)
  derives Codec.AsObject
