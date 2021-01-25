package net.kc5m.sagakka

import java.util.UUID

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Props, SupervisorStrategy}
import akka.cluster.typed.ClusterSingleton
import akka.cluster.typed.SingletonActor

object Main extends App {
  implicit val system: ActorSystem[Orchestrator.Command] = ActorSystem(Orchestrator(UUID.randomUUID()), "sagakka")
  val singletonManager = ClusterSingleton(system)
  val proxy: ActorRef[SomeGateway.Command] = singletonManager.init(
    SingletonActor(Behaviors.supervise(SomeGateway()).onFailure[Exception](SupervisorStrategy.restart), "GlobalCounter")
  )

  system ! Orchestrator.Init

}
