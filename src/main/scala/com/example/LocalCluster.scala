package com.example

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.cluster.Cluster
import akka.cluster.sharding.ShardRegion.{ ExtractEntityId, ExtractShardId }
import akka.cluster.sharding.{ ClusterSharding, ClusterShardingSettings }
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import com.typesafe.config.{ Config, ConfigFactory }

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.util.Random

object Node1 extends App {
  new Main(1)
}

object Node2 extends App {
  new Main(2)
}

object Node3 extends App {
  new Main(3)
}

class Main(nr: Int) {

  val config: Config = ConfigFactory.parseString(
    s"""
      akka.remote.artery.canonical.hostname = "127.0.0.$nr"
      akka.management.http.hostname = "127.0.0.$nr"
      akka.remote.netty.tcp.hostname = "127.0.0.$nr"
    """).withFallback(ConfigFactory.load())

  val system = ActorSystem("local-cluster", config)

  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  AkkaManagement(system).start()

  ClusterBootstrap(system).start()

  Cluster(system).registerOnMemberUp(system.log.info("Cluster is up!"))

  val numberOfShards = 15

  val extractEntityId: ExtractEntityId = {
    case msg @ Saludar(entityId) => (entityId.toString, msg)
  }

  val extractShardId: ExtractShardId = {
    case Saludar(entityId) => (entityId % numberOfShards).toString
  }

  val shardRegionManager: ActorRef = ClusterSharding(system).start(
    typeName = "saludador",
    entityProps = Props[Saludador],
    settings = ClusterShardingSettings(system),
    extractEntityId = extractEntityId,
    extractShardId = extractShardId)
  val random = new Random()
  system.scheduler.schedule(0.seconds, 2.seconds)(shardRegionManager ! Saludar(random.nextInt(100)))
}

