package com.example.agenticai.model.response

import io.circe.Codec

case class TextInfo(content: String, beginOffset: Int) 
  derives Codec.AsObject