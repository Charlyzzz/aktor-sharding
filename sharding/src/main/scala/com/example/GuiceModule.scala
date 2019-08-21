package com.example

import akka.actor.ActorRef
import aspects.MonitorAspect
import com.google.inject.AbstractModule
import org.aspectj.lang.Aspects

case class GuiceModule(actorSystemInterceptor: ActorRef) extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[ActorRef]).toInstance(actorSystemInterceptor)
    requestInjection(Aspects.aspectOf(classOf[MonitorAspect]))
  }
}
