package com.example.agenticai.model.response

import io.circe.Codec

case class Sentence(
    text: TextInfo,
    sentiment: Sentiment
) derives Codec.AsObject
