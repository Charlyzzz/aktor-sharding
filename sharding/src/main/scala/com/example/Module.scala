package com.example

import akka.actor.ActorRef
import aspects.MonitorAspect
import com.google.inject.AbstractModule
import org.aspectj.lang.Aspects

case class Module(actorRef: ActorRef) extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[ActorRef]).toInstance(actorRef)
    requestInjection(Aspects.aspectOf(classOf[MonitorAspect]))
  }
}
