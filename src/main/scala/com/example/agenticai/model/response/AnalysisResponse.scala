package com.example.agenticai.model.response

import io.circe.Codec

case class AnalysisResponse(
    sentences: List[Sentence],
    entities: List[Entity],
    documentSentiment: Sentiment,
    languageCode: String,
    categories: List[Category],
    moderationCategories: List[Category],
    languageSupported: Boolean
) derives Codec.AsObject
