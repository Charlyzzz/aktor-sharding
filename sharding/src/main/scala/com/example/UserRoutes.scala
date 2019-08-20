package com.example

import akka.NotUsed
import akka.actor.ActorSystem
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.adapter._
import akka.event.Logging
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.ws.{ Message, TextMessage }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.stream.scaladsl.{ Flow, Sink, Source }
import akka.stream.{ Materializer, OverflowStrategy }
import akka.util.Timeout

import scala.concurrent.ExecutionContext

trait UserRoutes extends SprayJsonSupport {

  implicit def system: ActorSystem

  implicit def excecutionContext: ExecutionContext = system.dispatcher

  implicit def materializer: Materializer

  lazy val log = Logging(system, classOf[UserRoutes])

  implicit def timeout: Timeout

  def greeter: Flow[Message, Message, Any] = Flow[Message].collectType[TextMessage]

  def keepAlive: Flow[Any, Message, NotUsed] = {
    val (webSocketOut, source) = Source.actorRef[Message](Int.MaxValue, OverflowStrategy.fail).preMaterialize()
    val userActor = system.spawnAnonymous(actors.user(webSocketOut))
    system.toTyped.receptionist ! Receptionist.register(actors.users, userActor)
    Flow.fromSinkAndSource(Sink.ignore, source)
  }

  lazy val userRoutes: Route =
    path("socket") {
      handleWebSocketMessages(keepAlive)
    }
}
