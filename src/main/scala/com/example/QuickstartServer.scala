package com.example

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.settings.ServerSettings
import akka.stream.scaladsl.Source
import akka.stream.{ ActorMaterializer, OverflowStrategy }
import akka.util.Timeout

import scala.concurrent.duration.{ Duration, _ }
import scala.concurrent.{ Await, Future }
import scala.util.{ Failure, Success }

object QuickstartServer extends UserRoutes {

  implicit val system: ActorSystem = ActorSystem("aktors-sharding")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  val (broadcasterQueue, wsSource) = Source.queue[Message](Integer.MAX_VALUE, OverflowStrategy.fail).preMaterialize()

  override lazy val timeout = Timeout(5.seconds)

  lazy val routes: Route = userRoutes

  private val defaultSettings = ServerSettings(system)
  private val wsSettings = defaultSettings
    .websocketSettings.withPeriodicKeepAliveMaxIdle(1.second)
  val customSettings = defaultSettings.withWebsocketSettings(wsSettings)
  val serverBinding: Future[Http.ServerBinding] = Http().bindAndHandle(routes, "localhost", 8080, settings = customSettings)

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
