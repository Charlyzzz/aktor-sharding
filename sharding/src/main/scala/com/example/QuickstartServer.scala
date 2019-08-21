package com.example

import akka.actor.{ Actor, ActorLogging, ActorSystem, Props }
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{ Message, TextMessage }
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.{ Source, SourceQueueWithComplete }
import akka.stream.{ ActorMaterializer, OverflowStrategy }
import akka.util.Timeout
import com.google.inject.Guice

import scala.concurrent.duration.{ Duration, _ }
import scala.concurrent.{ Await, Future }
import scala.util.{ Failure, Success }

object QuickstartServer extends App with UserRoutes {

  implicit val system: ActorSystem = ActorSystem("aktors-sharding")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  val (broadcasterQueue, wsSource) = Source.queue[Message](Integer.MAX_VALUE, OverflowStrategy.fail).preMaterialize()

  val systemInterceptor = system.actorOf(ActorSystemInterceptor.props(broadcasterQueue), ActorSystemInterceptor.name)
  Guice.createInjector(GuiceModule(systemInterceptor))

  override lazy val timeout = Timeout(5.seconds)

  system.scheduler.schedule(0.seconds, 1.second)(broadcasterQueue.offer(TextMessage("Hola!")))

  lazy val routes: Route = userRoutes

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

  Await.result(system.whenTerminated, Duration.Inf)
}

class ActorSystemInterceptor(val broadcasterQueue: SourceQueueWithComplete[Message]) extends Actor with ActorLogging {
  override def receive: Receive = {
    case x: String =>
      if (!x.contains(ActorSystemInterceptor.name)) {
        log.info(x)
        broadcasterQueue.offer(TextMessage(x))
      }
  }
}

object ActorSystemInterceptor {
  val name: String = "systemInterceptor"

  def props(broadcasterQueue: SourceQueueWithComplete[Message]): Props =
    Props(new ActorSystemInterceptor(broadcasterQueue))
}
