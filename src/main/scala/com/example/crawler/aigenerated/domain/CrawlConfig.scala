package com.example.crawler.aigenerated.domain

import scala.concurrent.duration.{Duration, DurationInt}

case class CrawlConfig(
    maxDepth: Int = 3,
    maxPages: Int = 100,
    delayBetweenRequests: Duration = 1.second,
    timeout: Duration = 10.seconds,
    allowedDomains: Set[String] = Set.empty,
    blockedDomains: Set[String] = Set.empty,
    userAgent: String = "ScalaCrawler/1.0",
    respectRobotsTxt: Boolean = true
)
