package com.example

import akka.actor.{ Actor, ActorLogging, PoisonPill, ReceiveTimeout }
import akka.cluster.sharding.ShardRegion

import scala.concurrent.duration._

class Saludador extends Actor with ActorLogging {

  context.setReceiveTimeout(15.seconds)

  override def receive: Receive = {
    case _ => log.info("Hola!")
    case ReceiveTimeout => context.parent.tell(ShardRegion.Passivate(PoisonPill), self)
  }
}
