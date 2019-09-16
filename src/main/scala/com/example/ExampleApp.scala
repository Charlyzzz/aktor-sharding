package com.example

import akka.actor.{ ActorRef, ActorSystem, PoisonPill, Props }
import akka.cluster.sharding.ShardRegion.{ ExtractEntityId, ExtractShardId }
import akka.cluster.sharding.{ ClusterSharding, ClusterShardingSettings }
import akka.cluster.singleton.{ ClusterSingletonManager, ClusterSingletonManagerSettings }
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement

object ExampleApp extends App {

  val system = ActorSystem()

  AkkaManagement(system).start()
  ClusterBootstrap(system).start()

  val numberOfShards = 15

  val extractEntityId: ExtractEntityId = {
    case msg @ Saludar(entityId) => (entityId.toString, msg)
  }

  val extractShardId: ExtractShardId = {
    case Saludar(entityId) => (entityId % numberOfShards).toString
  }

  val shardRegionManager: ActorRef = ClusterSharding(system).start(
    typeName = "entityName",
    entityProps = Props[Saludador],
    settings = ClusterShardingSettings(system),
    extractEntityId = extractEntityId,
    extractShardId = extractShardId)

  shardRegionManager ! Saludar(12)

  val singletonProps = ClusterSingletonManager.props(
    singletonProps = Props[ActorSingleton],
    terminationMessage = PoisonPill,
    settings = ClusterSingletonManagerSettings(system))

  system.actorOf(singletonProps, name = "Singleton")

}

case class Saludar(entityId: Int)

