package com.example

import akka.actor.{ Actor, ActorLogging, Props }
import akka.http.scaladsl.model.ws
import akka.http.scaladsl.model.ws.Message
import akka.stream.scaladsl.SourceQueueWithComplete
import com.example.event.Event
import spray.json.DefaultJsonProtocol

object MyJsonProtocol extends DefaultJsonProtocol {
  implicit val eventFormat = jsonFormat2(Event.apply)
}

class ActorSystemInterceptor(val broadcasterQueue: SourceQueueWithComplete[Message]) extends Actor with ActorLogging {

  override def receive: Receive = {

    case x: String =>
      if (isNotSelfEvent(x)) {
        x.splitAt(3) match {
          case ("UP|", address) => push(Event.up(address))
          case ("DN|", address) => push(Event.down(address))
          case invalidEvent =>
            log.warning(s"invalid event $invalidEvent")
        }
      }
  }

  private def push(event: Event) = {
    import MyJsonProtocol._
    import spray.json._
    val eventAsJsonString = event.toJson.toString
    broadcasterQueue.offer(ws.TextMessage(eventAsJsonString))
  }

  private def isNotSelfEvent(x: String) = !x.contains(ActorSystemInterceptor.name)
}

object ActorSystemInterceptor {
  val name: String = "systemInterceptor"

  def props(broadcasterQueue: SourceQueueWithComplete[Message]): Props =
    Props(new ActorSystemInterceptor(broadcasterQueue))
}
