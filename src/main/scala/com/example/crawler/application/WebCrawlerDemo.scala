package com.example.crawler.application

import cats.effect.{Clock, ExitCode, IO, IOApp}
import com.example.crawler.application.crawlers.application.CrawlParameters
import com.example.crawler.application.crawlers.application.parallel.ParallelCrawler
import org.http4s.Uri
import org.http4s.Uri.Path

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.{Duration, DurationInt, FiniteDuration}
import scala.util.{Success, Try}

object WebCrawlerDemo extends IOApp:

  override def run(args: List[String]): IO[ExitCode] =
    val program = for
      _ <- IO.println("Welcome to web crawling!")

      // TODO implement reading from configuration file
      parameters <-
        if args.isEmpty then printUsage() >> askInputParams()
        else argsToParameters(args)

      crawlerType <-
        if args.isEmpty then
          for
            _ <- IO.println(
              """Please select the Crawler [Parallel]:
                  |[1] Parallel
                  |""".stripMargin
            )
            string <- IO.readLine
          yield string
        else IO.pure(args.lift(3).getOrElse("Parallel"))

      crawler = chooseCrawler(parameters.parallelism, parameters.delay)(crawlerType)

      _ <- printStartingWithParameters(parameters)

      (took, links) <- Clock[IO].timed(crawler.crawl(parameters.baseUri))

      result = links.map { case (path, links) =>
        parameters.baseUri.withPath(path).toString -> links.mkString(",")
      }

      // TODO implement pretty print logic, something like nested folder
      _ <- IO.println(result)

      _ <- IO.println(s"Time taken: ${took.toUnit(TimeUnit.SECONDS)}")
    yield ExitCode.Success

    program.recoverWith { error =>
      IO.println(error.getMessage) >> IO.pure(ExitCode.Error)
    }

  private def argsToParameters(args: List[String]) =
    validateBaseUri(args.head).map { domain =>
      val maybeParallelismString = args.lift(1)
      val maybeParallelismInt    = maybeParallelismString.flatMap(p => Try(p.toInt).toOption)
      val parallelism            = maybeParallelismInt.getOrElse(2)

      val maybeRateString = args.lift(2)
      val maybeRate       = maybeRateString.flatMap(parseRate)
      val rate            = maybeRate.getOrElse(1000.millis)

      CrawlParameters(domain, parallelism, rate)
    }

  private def askInputParams() = for
    _           <- IO.println("Please insert the starting domain [monzo.com]: ")
    baseUri     <- IO.readLine.flatMap(validateBaseUri).map(_.withPath(Path.empty))
    _           <- IO.println("Please insert parallelism [2]: ")
    parallelism <- IO.readLine.map { p => Try(p.toInt).getOrElse(2) }
    _           <- IO.println("Please insert delay between groups of requests (length.unit) [1000.millis]: ")
    rate        <- IO.readLine.map { r => parseRate(r).getOrElse(1000.millis) }
  yield CrawlParameters(baseUri, parallelism, rate)

  private def chooseCrawler(parallelism: Int, rate: FiniteDuration)(string: String) =
    string match
      case _ => ParallelCrawler(parallelism, rate)

  private def printUsage() =
    IO.println(
      """Usage instructions:
        |For direct usage, please input the URL and optionally the parallelism, the rate and the type of Crawler.
        |  java -jar out/webcrawler/assembly.dest/out.jar monzo.com 2 100.millis Parallel
        |""".stripMargin
    )

  private def printStartingWithParameters(parameters: CrawlParameters) =
    val rate =
      if parameters.delay.length > 0 then
        val requestsPerSecond = 1_000_000_000L / parameters.delay.toUnit(TimeUnit.NANOSECONDS).longValue
        val rate              = parameters.parallelism * requestsPerSecond
        s"$rate req/s"
      else "Uncapped"

    IO.println(
      s"""Started crawling with parameters:
        |URL: ${parameters.baseUri}
        |Parallelism: ${parameters.parallelism}
        |Rate: $rate
        |""".stripMargin
    )

  private def validateBaseUri(uri: String) =
    IO.fromTry(
      if !uri.isBlank then
        if uri.startsWith("http") then Uri.fromString(uri).toTry
        else Uri.fromString(s"https://$uri").toTry
      else Success(Uri.unsafeFromString("https://monzo.com"))
    )

  private def parseRate(rate: String): Option[FiniteDuration] =
    Try(Duration.create(rate)).toOption.collect { case fd: FiniteDuration => fd }
