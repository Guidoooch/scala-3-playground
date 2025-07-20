package com.example.crawler.application.crawlers.application

import org.http4s.Uri

import scala.concurrent.duration.FiniteDuration

case class CrawlParameters(
    baseUri: Uri,
    parallelism: Int,
    delay: FiniteDuration
)
