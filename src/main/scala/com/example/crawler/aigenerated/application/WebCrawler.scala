package com.example.crawler.aigenerated.application

import com.example.crawler.aigenerated.domain.{CrawlConfig, CrawlResult}

import java.net.{HttpURLConnection, URI}
import java.time.LocalDateTime
import scala.collection.mutable
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source
import scala.util.Try

class WebCrawler(config: CrawlConfig)(implicit ec: ExecutionContext) {

  private val visited     = mutable.Set[String]()
  private val crawlQueue  = mutable.Queue[(String, Int)]()     // (url, depth)
  private val results     = mutable.ListBuffer[CrawlResult]()
  private val robotsCache = mutable.Map[String, Set[String]]() // domain -> disallowed paths

  // URL validation and normalization
  private def normalizeUrl(url: String): Option[String] = {
    Try {
      val uri = new URI(url)
      if (uri.getScheme == null) {
        new URI("https", uri.getAuthority, uri.getPath, uri.getQuery, uri.getFragment).toString
      } else {
        uri.normalize().toString
      }
    }.toOption
  }

  private def isValidUrl(url: String): Boolean = {
    Try {
      val uri    = new URI(url)
      val scheme = uri.getScheme
      val host   = uri.getHost

      scheme != null && (scheme == "http" || scheme == "https") &&
      host != null && !host.isEmpty &&
      !url.contains("#") // Skip fragments
    }.getOrElse(false)
  }

  private def isDomainAllowed(url: String): Boolean = {
    Try {
      val host = new URI(url).getHost.toLowerCase

      val domainAllowed = if (config.allowedDomains.nonEmpty) {
        config.allowedDomains.exists(domain => host.contains(domain.toLowerCase))
      } else true

      val domainNotBlocked = !config.blockedDomains.exists(domain => host.contains(domain.toLowerCase))

      domainAllowed && domainNotBlocked
    }.getOrElse(false)
  }

  // Robots.txt handling
  private def fetchRobotsTxt(domain: String): Future[Set[String]] = {
    if (!config.respectRobotsTxt) {
      Future.successful(Set.empty)
    } else {
      robotsCache.get(domain) match {
        case Some(disallowed) => Future.successful(disallowed)
        case None =>
          val robotsUrl = s"https://$domain/robots.txt"
          fetchContent(robotsUrl)
            .map { result =>
              val disallowed = parseRobotsTxt(result.content)
              robotsCache(domain) = disallowed
              disallowed
            }
            .recover { case _ =>
              robotsCache(domain) = Set.empty
              Set.empty
            }
      }
    }
  }

  private def parseRobotsTxt(content: String): Set[String] = {
    val disallowPattern  = """Disallow:\s*(.+)""".r
    val userAgentPattern = """User-agent:\s*(.+)""".r

    val lines             = content.split("\n").map(_.trim)
    var isRelevantSection = false
    val disallowed        = mutable.Set[String]()

    for (line <- lines) {
      line match {
        case userAgentPattern(agent) =>
          isRelevantSection = agent == "*" || agent.toLowerCase.contains("scalacrawler")
        case disallowPattern(path) if isRelevantSection =>
          disallowed += path.trim
        case _ =>
      }
    }

    disallowed.toSet
  }

  private def isAllowedByRobots(url: String, disallowed: Set[String]): Boolean = {
    if (disallowed.isEmpty) return true

    Try {
      val path = new URI(url).getPath
      !disallowed.exists(blocked => path.startsWith(blocked))
    }.getOrElse(true)
  }

  // HTTP client methods
  private def fetchContent(url: String): Future[CrawlResult] = {
    val future = Future.fromTry {
      Try {
        val connection = new URI(url).toURL.openConnection().asInstanceOf[HttpURLConnection]
        connection.setRequestMethod("GET")
        connection.setRequestProperty("User-Agent", config.userAgent)
        connection.setConnectTimeout(config.timeout.toMillis.toInt)
        connection.setReadTimeout(config.timeout.toMillis.toInt)

        val statusCode = connection.getResponseCode

        if (statusCode == 200) {
          val content = Source.fromInputStream(connection.getInputStream, "UTF-8").mkString
          val title   = extractTitle(content)
          val links   = extractLinks(content, url)

          CrawlResult(
            url = url,
            title = title,
            content = content,
            links = links,
            timestamp = LocalDateTime.now(),
            statusCode = statusCode
          )
        } else {
          CrawlResult(
            url = url,
            title = None,
            content = "",
            links = List.empty,
            timestamp = LocalDateTime.now(),
            statusCode = statusCode,
            error = Some(s"HTTP $statusCode")
          )
        }
      }
    }

    future.recover:
      case exception =>
        CrawlResult(
          url = url,
          title = None,
          content = "",
          links = List.empty,
          timestamp = LocalDateTime.now(),
          statusCode = 0,
          error = Some(exception.getMessage)
        )
  }

  // HTML parsing methods
  private def extractTitle(html: String): Option[String] = {
    val titlePattern = """<title[^>]*>(.*?)</title>""".r
    titlePattern.findFirstMatchIn(html.toLowerCase) match {
      case Some(m) => Some(m.group(1).trim)
      case None    => None
    }
  }

  private def extractLinks(html: String, baseUrl: String): List[String] = {
    val linkPattern = """<a[^>]*href\s*=\s*["']([^"']+)["']""".r
    val links       = linkPattern.findAllMatchIn(html).map(_.group(1)).toList

    links.flatMap { link =>
      resolveUrl(link, baseUrl).filter(isValidUrl)
    }.distinct
  }

  private def resolveUrl(link: String, baseUrl: String): Option[String] = {
    Try {
      val base     = new URI(baseUrl)
      val resolved = base.resolve(link)
      normalizeUrl(resolved.toString)
    }.toOption.flatten
  }

  private def crawlRecursive(): Future[List[CrawlResult]] = {
    if (crawlQueue.isEmpty || results.size >= config.maxPages) {
      Future.successful(results.toList)
    } else {
      val (url, depth) = crawlQueue.dequeue()

      if (visited.contains(url) || depth > config.maxDepth || !isDomainAllowed(url)) {
        crawlRecursive()
      } else {
        visited += url

        val domain = Try(new URI(url).getHost).getOrElse("")

        for {
          robotsDisallowed <- fetchRobotsTxt(domain)
          result <-
            if (isAllowedByRobots(url, robotsDisallowed)) {
              println(s"Crawling: $url (depth: $depth)")
              fetchContent(url)
            } else {
              println(s"Blocked by robots.txt: $url")
              Future.successful(
                CrawlResult(
                  url,
                  None,
                  "",
                  List.empty,
                  LocalDateTime.now(),
                  0,
                  Some("Blocked by robots.txt")
                )
              )
            }
          _ = results += result
          _ = if (result.error.isEmpty && depth < config.maxDepth) {
            result.links.foreach { link =>
              if (!visited.contains(link)) {
                crawlQueue.enqueue((link, depth + 1))
              }
            }
          }
          _ <- Future {
            Thread.sleep(config.delayBetweenRequests.toMillis)
          }
          finalResults <- crawlRecursive()
        } yield finalResults
      }
    }
  }

  // Main crawling logic
  def crawl(startUrl: String): Future[List[CrawlResult]] = {
    normalizeUrl(startUrl) match {
      case Some(url) =>
        crawlQueue.enqueue((url, 0))
        crawlRecursive()
      case None =>
        Future.successful(
          List(
            CrawlResult(
              startUrl,
              None,
              "",
              List.empty,
              LocalDateTime.now(),
              0,
              Some("Invalid start URL")
            )
          )
        )
    }
  }

  // Utility methods
  def getVisitedUrls: Set[String] = visited.toSet
  def getQueueSize: Int           = crawlQueue.size
  def getResultsCount: Int        = results.size

  // Export results
  def exportToCSV(filename: String): Unit = {
    import java.io.PrintWriter
    val writer = new PrintWriter(filename)

    writer.println("URL,Title,Status Code,Links Count,Timestamp,Error")
    results.foreach { result =>
      val title = result.title.getOrElse("").replace(",", ";")
      val error = result.error.getOrElse("").replace(",", ";")
      writer.println(s"${result.url},$title,${result.statusCode},${result.links.size},${result.timestamp},$error")
    }

    writer.close()
    println(s"Results exported to $filename")
  }
}

object WebCrawler {

  def apply(config: CrawlConfig = CrawlConfig())(implicit ec: ExecutionContext): WebCrawler = {
    new WebCrawler(config)
  }

  // Pre-configured crawlers
  def conservative(implicit ec: ExecutionContext): WebCrawler = {
    WebCrawler(
      CrawlConfig(
        maxDepth = 2,
        maxPages = 50,
        delayBetweenRequests = 2.seconds,
        respectRobotsTxt = true
      )
    )
  }

  def aggressive(implicit ec: ExecutionContext): WebCrawler = {
    WebCrawler(
      CrawlConfig(
        maxDepth = 5,
        maxPages = 500,
        delayBetweenRequests = 500.milliseconds,
        respectRobotsTxt = false
      )
    )
  }

  def domainRestricted(domains: Set[String])(implicit ec: ExecutionContext): WebCrawler = {
    WebCrawler(
      CrawlConfig(
        allowedDomains = domains,
        maxDepth = 4,
        maxPages = 200
      )
    )
  }
}
