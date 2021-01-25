package net.kc5m.sagakka

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, SupervisorStrategy}
import akka.cluster.typed.ClusterSingleton
import akka.cluster.typed.SingletonActor

import java.util.UUID

object Main extends App {
  implicit val system: ActorSystem[Init] = ActorSystem(Orchestrator(), "sagakka")
  val singletonManager = ClusterSingleton(system)
  val proxy: ActorRef[SomeGateway.Command] = singletonManager.init(
    SingletonActor(Behaviors.supervise(SomeGateway()).onFailure[Exception](SupervisorStrategy.restart), "GlobalCounter")
  )

  system ! Init(UUID.randomUUID())

}
