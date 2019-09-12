package com.example

import akka.actor.{Actor, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.settings.ServerSettings
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import akka.stream.scaladsl.Source
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.util.Timeout
import com.google.inject.Guice
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

object ShardingApp extends App with UserRoutes {

  AkkaManagement(system).start()

  // Starting the bootstrap process needs to be done explicitly
  ClusterBootstrap(system).start()

  startup(Seq("2551", "2552", "0"))

  implicit val system: ActorSystem = ActorSystem("aktors-sharding")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  val (broadcasterQueue, wsSource) = Source.queue[Message](Integer.MAX_VALUE, OverflowStrategy.fail).preMaterialize()

  val systemInterceptor = system.actorOf(ActorSystemInterceptor.props(broadcasterQueue), ActorSystemInterceptor.name)
  Guice.createInjector(GuiceModule(systemInterceptor))

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

  def startup(ports: Seq[String]): Unit = {
    // In a production application you wouldn't typically start multiple ActorSystem instances in the
    // same JVM, here we do it to easily demonstrate these ActorSytems (which would be in separate JVM's)
    // talking to each other.
    ports foreach {
      port =>
        // Override the configuration of the port
        val config = ConfigFactory.parseString("akka.remote.artery.canonical.port=" + port)
          .withFallback(ConfigFactory.load())

        // Create an Akka system
        val system = ActorSystem("ShardingSystem", config)
        system.actorOf(Props(new Actor {
          override def receive: Receive = Actor.ignoringBehavior
        }))
        // Create an actor that starts the sharding and sends random messages
        system.actorOf(Props[Devices])
    }
  }

}

