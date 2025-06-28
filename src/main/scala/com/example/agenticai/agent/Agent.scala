package com.example.agenticai.agent

trait Agent[F[_], State, Percept, Action] {
  def receive(percept: Percept): F[Unit]
  def getState: F[State]
}
