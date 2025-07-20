package com.example.crawler.aigenerated

import com.example.crawler.aigenerated.application.WebCrawler
import com.example.crawler.aigenerated.domain.{CrawlConfig, CrawlResult}

import java.net.URI
import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.*
import scala.util.{Failure, Success, Try}

// Statistics and analysis utilities
object CrawlAnalyzer {

  def analyze(results: List[CrawlResult]): Unit = {
    val total      = results.size
    val successful = results.count(_.error.isEmpty)
    val failed     = results.count(_.error.isDefined)

    val statusCodes = results.groupBy(_.statusCode).map { case (code, list) =>
      code -> list.size
    }

    val domains = results
      .map(_.url)
      .map { url =>
        Try(new URI(url).getHost).getOrElse("unknown")
      }
      .groupBy(identity)
      .map { case (domain, list) =>
        domain -> list.size
      }

    println(s"\n=== Crawl Analysis ===")
    println(s"Total URLs: $total")
    println(s"Successful: $successful")
    println(s"Failed: $failed")
    println(s"Success rate: ${(successful.toDouble / total * 100).round}%")

    println(s"\nStatus codes:")
    statusCodes.toSeq.sortBy(_._1).foreach { case (code, count) =>
      println(s"  $code: $count")
    }

    println(s"\nDomains:")
    domains.toSeq.sortBy(-_._2).take(10).foreach { case (domain, count) =>
      println(s"  $domain: $count")
    }

    val avgLinksPerPage = results.map(_.links.size).sum.toDouble / results.size
    println(f"\nAverage links per page: $avgLinksPerPage%.1f")
  }

  def findBrokenLinks(results: List[CrawlResult]): List[CrawlResult] = {
    results.filter(r => r.statusCode >= 400 || r.error.isDefined)
  }

  def getLinkGraph(results: List[CrawlResult]): Map[String, List[String]] = {
    results.map(r => r.url -> r.links).toMap
  }
}

// Example usage and demo
object WebCrawlerDemo extends App {
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(4))

  private def demo(startUrl: String): Unit = {
    println("=== Web Crawler Demo ===")

    // Example 1: Basic crawling
    val basicCrawler = WebCrawler.conservative

    println(s"Starting crawl from: $startUrl")

    val crawlFuture = basicCrawler.crawl(startUrl)

    crawlFuture.onComplete {
      case Success(results) =>
        println(s"\nCrawl completed! Found ${results.size} pages")
        CrawlAnalyzer.analyze(results)

        // Export results
        basicCrawler.exportToCSV("crawl_results.csv")

        // Find broken links
        val brokenLinks = CrawlAnalyzer.findBrokenLinks(results)
        if (brokenLinks.nonEmpty) {
          println(s"\nFound ${brokenLinks.size} broken links:")
          brokenLinks.take(5).foreach { result =>
            println(s"  ${result.url}: ${result.error.getOrElse(s"HTTP ${result.statusCode}")}")
          }
        }

      case Failure(exception) =>
        println(s"Crawl failed: ${exception.getMessage}")
    }

    // Wait for completion (in real app, you'd handle this differently)
    Thread.sleep(30000)

    // Example 2: Domain-restricted crawling
    println("\n=== Domain-restricted crawling ===")
    val domainCrawler = WebCrawler.domainRestricted(Set("example.com", "example.org"))

    // Example 3: Custom configuration
    val customConfig = CrawlConfig(
      maxDepth = 3,
      maxPages = 100,
      delayBetweenRequests = 1.second,
      allowedDomains = Set("news.ycombinator.com"),
      userAgent = "MyBot/1.0",
      respectRobotsTxt = true
    )

    val customCrawler = WebCrawler(customConfig)
  }

  // Uncomment to run demo
  demo("https://google.com")
}
