package com.example

import akka.actor.{ Actor, ActorLogging, Props }
import akka.http.scaladsl.model.ws.{ Message, TextMessage }
import akka.stream.scaladsl.SourceQueueWithComplete

sealed trait Messages

case class Broadcast(message: String) extends Messages

class User(val broadcaster: SourceQueueWithComplete[Message]) extends Actor with ActorLogging {
  override def receive: Receive = {
    case Broadcast(message) => broadcaster.offer(TextMessage(message))
  }
}

object User {
  def props(broadcaster: SourceQueueWithComplete[Message]): Props = Props(new User(broadcaster))
}
