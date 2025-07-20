package com.example.crawler.application.common

object UriUtils:

  inline def isNotPageLink(href: String): Boolean = isFragmentIdentifier(href) || isMediaFile(href)

  inline private def isFragmentIdentifier(string: String) = string.contains("#")

  inline private def isHtmlPage(href: String): Boolean = href.takeRight(4).contains("htm")

  inline private def isMediaFile(string: String) = {
    val isFile = string.lastIndexOf(".") > string.lastIndexOf("/")
    isFile && !isHtmlPage(string)
  }
