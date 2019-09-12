package com.example

import akka.actor.{Actor, ActorLogging, PoisonPill, ReceiveTimeout}
import akka.cluster.sharding.ShardRegion

import scala.concurrent.duration._

class EntityActor extends Actor with ActorLogging {

  context.setReceiveTimeout(15.seconds)

  override def receive: Receive = {
    case ReceiveTimeout => context.parent.tell(ShardRegion.Passivate(PoisonPill), self)
  }
}
