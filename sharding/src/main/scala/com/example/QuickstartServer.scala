package com.example

import akka.NotUsed
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.receptionist.Receptionist.Listing
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.google.inject.Guice

import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

sealed trait Broadcast

case class FoundUsers(users: Listing) extends Broadcast

case object StartBroadcast extends Broadcast

object QuickstartServer extends App with UserRoutes {

  implicit val system: ActorSystem = ActorSystem("aktors-sharding")
  val b = system.actorOf(Props[B], "test-b")
  private val injector = Guice.createInjector(Module(b))
  val a = injector.getInstance(classOf[akka.actor.ActorRef])
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  override lazy val timeout = Timeout(5.seconds)

  lazy val routes: Route = userRoutes

  val broadcast: Behavior[NotUsed] = Behaviors.setup[Any](ctx => {
    val receptionistAdapter: ActorRef[Listing] = ctx.messageAdapter(FoundUsers)
    Behaviors.withTimers(timer => {
      timer.startPeriodicTimer(null, StartBroadcast, 3.seconds)
      Behaviors.receiveMessage {
        case StartBroadcast =>
          ctx.system.receptionist ! Receptionist.find(actors.users, receptionistAdapter)
          //println(ctx.system.printTree)
          Behaviors.same
        case FoundUsers(actors.users.Listing(users)) =>
          users.foreach(_ ! A)
          Behaviors.same
      }
    })
  }).narrow

  system.spawn(broadcast, "broadcaster")

  val serverBinding: Future[Http.ServerBinding] = Http().bindAndHandle(routes, "localhost", 8080)

  serverBinding.onComplete {
    case Success(bound) =>
      println(s"Server online at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/")
    case Failure(e) =>
      Console.err.println(s"Server could not start!")
      e.printStackTrace()
      system.terminate()
  }

  scala.sys.addShutdownHook {
    system.terminate()
    Await.result(system.whenTerminated, 30.seconds)
  }

  val logger = system.spawnAnonymous(Behaviors.logMessages(Behaviors.ignore))
  system.eventStream.subscribe(logger.toUntyped, classOf[Any])

  Await.result(system.whenTerminated, Duration.Inf)
}

class B extends Actor with ActorLogging {
  override def receive: Receive = {
    case x => log.info(s"Msg{$x}")
  }
}
