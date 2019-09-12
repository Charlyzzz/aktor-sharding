package com.example

import akka.actor.{ Actor, ActorLogging, Props }
import akka.http.scaladsl.model.ws.{ Message, TextMessage }
import akka.stream.QueueOfferResult
import akka.stream.scaladsl.SourceQueueWithComplete
import com.example.event.Event

import scala.concurrent.Future
import scala.util.Try

class ActorSystemInterceptor(val broadcasterQueue: SourceQueueWithComplete[Message]) extends Actor with ActorLogging {

  override def receive: Receive = {
    case x: String =>
      if (isNotSelfEvent(x)) {
        x.splitAt(3) match {
          case ("UP|", address) => Try(push(Event.up(address)))
          case ("DN|", address) => Try(push(Event.down(address)))
          case invalidEvent =>
            log.warning(s"invalid event $invalidEvent")
        }
      }
  }

  private def push(event: Event): Future[QueueOfferResult] = {
    import json.Protocol._
    import spray.json._

    val eventAsJsonString = event.toJson.toString
    broadcasterQueue.offer(TextMessage(eventAsJsonString))
  }

  private def isNotSelfEvent(x: String) = !x.contains(ActorSystemInterceptor.name)
}

object ActorSystemInterceptor {
  val name: String = "systemInterceptor"

  def props(broadcasterQueue: SourceQueueWithComplete[Message]): Props =
    Props(new ActorSystemInterceptor(broadcasterQueue))
}
