package com.example.http4s

import cats.effect.IO
import io.circe.Codec
import org.http4s.HttpRoutes
import org.http4s.dsl.io.*
import org.http4s.circe.CirceEntityCodec.*

object TweetRestApi:
  private case class Tweet(id: Int, message: String) derives Codec.AsObject

  private def getTweet(tweetId: Int): IO[Tweet] = IO(Tweet(tweetId, s"Tweet message for ID $tweetId"))

  private def getPopularTweets(): IO[Seq[Tweet]] = IO(Seq.tabulate(3)(i => Tweet(i, s"Popular tweet message $i")))

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "tweets" / "popular" =>
      getPopularTweets().flatMap(Ok(_))
    case GET -> Root / "tweets" / IntVar(tweetId) =>
      getTweet(tweetId).flatMap(Ok(_))
  }
