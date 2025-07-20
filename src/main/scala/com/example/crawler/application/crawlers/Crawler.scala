package com.example.crawler.application.crawlers

import org.http4s.Uri
import org.http4s.Uri.Path

trait Crawler[F[_]]:

  def crawl(baseUri: Uri): F[Map[Path, Set[String]]]
