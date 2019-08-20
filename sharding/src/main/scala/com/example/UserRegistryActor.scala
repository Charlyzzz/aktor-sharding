package com.example

import akka.actor.typed.receptionist.{ Receptionist, ServiceKey }
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, Behavior, Terminated }
import akka.http.scaladsl.model.ws.TextMessage

sealed trait UserMessages

case object A extends UserMessages

object actors {

  val users: ServiceKey[UserMessages] = ServiceKey("users")

  def user(websocketOut: ActorRef[TextMessage]): Behavior[UserMessages] = Behaviors.setup(ctx => {
    ctx.system.receptionist ! Receptionist.register(users, ctx.self)
    ctx.watch(websocketOut)

    Behaviors.receive[UserMessages]((_, msg) =>
      msg match {
        case A =>
          websocketOut ! TextMessage("keep alive")
          Behaviors.same
      }).receiveSignal { case (_, _: Terminated) => Behaviors.stopped }
  })
}

