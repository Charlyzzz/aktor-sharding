package com.example

import akka.NotUsed
import akka.actor.{ Actor, ActorLogging, ActorSystem, Props }
import akka.event.Logging
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.stream.scaladsl.{ Flow, Sink, Source }
import akka.util.Timeout

import scala.concurrent.ExecutionContext

trait UserRoutes extends SprayJsonSupport {

  implicit def system: ActorSystem

  implicit def excecutionContext: ExecutionContext = system.dispatcher

  lazy val log = Logging(system, classOf[UserRoutes])

  implicit def timeout: Timeout

  val wsSource: Source[Message, NotUsed]

  def keepAlive: Flow[Any, Message, NotUsed] = {
    system.actorOf(Props(new Actor with ActorLogging {
      override def receive: Receive = Actor.ignoringBehavior
    }))
    Flow.fromSinkAndSource(Sink.ignore, wsSource)
  }

  lazy val userRoutes: Route =
    concat(
      path("socket") {
        handleWebSocketMessages(keepAlive)
      },
      pathSingleSlash {
        getFromResource("index.html")
      })
}
