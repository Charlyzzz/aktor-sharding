package com.example

import akka.actor.{ActorRef, ActorSystem, PoisonPill, Props}
import akka.cluster.sharding.ShardRegion.ExtractShardId
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings}
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement

object ExampleApp extends App {

  val system = ActorSystem()

  AkkaManagement(system).start()
  ClusterBootstrap(system).start()


  val extractEntityId = {
    case Accion(entityId) => entityId
  }

  val numberOfShards = 15

  val extractShardId: ExtractShardId = {
    case Accion(entityId) => (entityId % numberOfShards).toString
  }

  val shardRegion: ActorRef = ClusterSharding(system).start(
    typeName = "entityName",
    entityProps = Props[EntityActor],
    settings = ClusterShardingSettings(system),
    extractEntityId = extractEntityId,
    extractShardId = extractShardId)

  shardRegion ! Accion(12)

  val singletonProps = ClusterSingletonManager.props(
    singletonProps = Props[ActorSingleton],
    terminationMessage = PoisonPill,
    settings = ClusterSingletonManagerSettings(system))

  system.actorOf(singletonProps, name = "Singleton")


}

case class Accion(entityId: Int)

