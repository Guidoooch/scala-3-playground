package com.example.crawler.application.crawlers.application.parallel

import cats.effect.IO
import com.example.crawler.application.common.LinkFinder
import com.example.crawler.application.crawlers.Crawler
import net.ruippeixotog.scalascraper.browser.{Browser, JsoupBrowser}
import org.http4s.Uri
import org.http4s.Uri.Path

import scala.concurrent.duration.FiniteDuration

// TODO I think this can be improved
class ParallelCrawler(parallelism: Int, delay: FiniteDuration) extends Crawler[IO]:

  override def crawl(baseUri: Uri): IO[Map[Path, Set[String]]] =
    def recursiveCrawl(linksToVisit: Set[Path], linksVisited: Map[Path, Set[String]]): IO[Map[Path, Set[String]]] =
      if linksToVisit.nonEmpty then
        val findLinksInDocument =
          IO.parTraverseN(parallelism)(linksToVisit.toSeq) { path =>
            val pageUri = baseUri.withPath(path)
            getDocument(pageUri)
              .map(document => path -> LinkFinder.findLinksInDocument(document))
              .recover(_ => path -> Set.empty[String])
          }.map(_.toMap)

        findLinksInDocument.flatMap { linksInDocument =>
          val documentLinksToVisit = LinkFinder.findRelativeLinks(linksInDocument.values.flatten.toSet, baseUri)
          val newLinksVisited      = linksInDocument ++ linksVisited
          val newLinksToVisit      = documentLinksToVisit diff newLinksVisited.keySet

          recursiveCrawl(newLinksToVisit, newLinksVisited).delayBy(delay)
        }
      else IO.pure(linksVisited)

    recursiveCrawl(Set(Path.Root), Map.empty)

  private val browser: Browser = JsoupBrowser()

  private def getDocument(uri: Uri): IO[browser.DocumentType] = IO.blocking(browser.get(uri.toString))

object ParallelCrawler:

  def apply(parallelism: Int, rate: FiniteDuration): ParallelCrawler = new ParallelCrawler(parallelism, rate)
