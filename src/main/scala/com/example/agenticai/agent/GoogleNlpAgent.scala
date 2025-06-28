package com.example.agenticai.agent

import cats.effect.*
import cats.effect.kernel.{Async, Ref}
import cats.syntax.all.*
import com.example.agenticai.client.AIClient
import com.example.agenticai.model.request.{AnnotateTextRequest, Document, Features}
import com.example.agenticai.model.response.AnalysisResponse
import org.http4s.circe.CirceEntityCodec.*

// Agent with AI integration
class GoogleNlpAgent[F[_]: Async](
    stateRef: Ref[F, Int],
    aiClient: AIClient[F]
):

  def receive(percept: String): F[AnalysisResponse] =
    for {
      currentState <- stateRef.get
      result       <- decide(currentState, percept)
      (newState, response) = result
      _ <- stateRef.set(newState)
    } yield response

  def getState: F[Int] = stateRef.get

  private def decide(state: Int, text: String): F[(Int, AnalysisResponse)] = {
    val document = Document("PLAIN_TEXT", text, Some("en"))
    val features = Features.All
    val entity   = AnnotateTextRequest(document, features, Some("UTF8"))

    for {
      analysis <- aiClient.annotateText[AnnotateTextRequest, AnalysisResponse](entity)
      newState = state + 1
    } yield (newState, analysis)
  }

object GoogleNlpAgent:
  def create[F[_]: Async](
      initialState: Int,
      aiClient: AIClient[F]
  ): F[GoogleNlpAgent[F]] =
    Ref.of[F, Int](initialState).map(new GoogleNlpAgent[F](_, aiClient))

