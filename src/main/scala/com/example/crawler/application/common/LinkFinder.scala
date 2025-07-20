package com.example.crawler.application.common

import net.ruippeixotog.scalascraper.dsl.DSL.*
import net.ruippeixotog.scalascraper.model.Document
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.http4s.Uri
import org.http4s.Uri.Path

object LinkFinder:

  inline def findLinksInDocument(document: Document): Set[String] =
    val items = document >> elementList("a")
    val hrefs = items.flatMap(_.attrs.get("href"))
    hrefs.toSet

  inline def findRelativeLinks(hrefs: Set[String], domain: Uri): Set[Path] =
    val pageLinksRelativeToDomain =
      hrefs
        .filterNot(UriUtils.isNotPageLink)
        .flatMap { href =>
          val trimmed = href.trim
          val safe    = Uri.encode(Uri.decode(trimmed))
          val result  = Uri.fromString(safe)

          result match
            case Left(_) => None

            case Right(linkUri) =>
              val maybeUriToVisit =
                if linkUri.path.absolute then
                  if linkUri.host == domain.host || linkUri.host.isEmpty then
                    Option.when(linkUri.path != domain.path)(linkUri.path)
                  else None
                else
                  val parent   = Path(domain.path.segments.dropRight(1))
                  val relative = parent.concat(linkUri.path).toAbsolute
                  Some(relative)

              maybeUriToVisit
        }
        .toSet

    pageLinksRelativeToDomain
