package com.example.agenticai

import cats.effect.*
import com.example.*
import com.example.agenticai.agent.GoogleNlpAgent
import com.example.agenticai.client.Http4sAIClient
import org.http4s.Uri
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.implicits.uri

// Example usage
object AgenticAIWithNLP extends IOApp {
  private val apiKey = "AIzaSyCCFW57HxnD4--9yjnLLJhHKGq23o94A-o"
  private val apiUri = uri"https://language.googleapis.com/v2/documents:annotateText".withQueryParam("key", apiKey)

  def run(args: List[String]): IO[ExitCode] =
    EmberClientBuilder.default[IO].build.use { client =>
      val aiClient = new Http4sAIClient[IO](client, apiUri)

      for {
        agent    <- GoogleNlpAgent.create[IO](0, aiClient)
        response <- agent.receive("What the HELL is happening with the financial markets right now!!!!")
        _        <- IO.println(s"Agent response:\n$response")
        state    <- agent.getState
        _        <- IO.println(s"Agent final state: $state")
      } yield ExitCode.Success
    }

}
