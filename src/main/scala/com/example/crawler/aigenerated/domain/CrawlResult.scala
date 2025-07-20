package com.example.crawler.aigenerated.domain

import java.time.LocalDateTime

case class CrawlResult(
    url: String,
    title: Option[String],
    content: String,
    links: List[String],
    timestamp: LocalDateTime,
    statusCode: Int,
    error: Option[String] = None
)
