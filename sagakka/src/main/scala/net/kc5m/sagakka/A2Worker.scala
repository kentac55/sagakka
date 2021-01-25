package net.kc5m.sagakka

import scala.util.Random

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object A2Worker {
  def apply(): Behavior[A2Req] =
    Behaviors.receive { (ctx, msg) =>
      ctx.log.info("id: {}", msg.id)

      Random.nextInt(10) match {
        case x if x < 8 =>
          Main.proxy ! SomeGateway.Increment
          msg.replyTo ! Orchestrator.A2Ok(id = msg.id, result = 0)
        case _ =>
          ctx.log.warn("BOOM! at A2")
          msg.replyTo ! Orchestrator.A2Ng(id = msg.id, new Exception("A2死にました"))
      }

      Behaviors.same
    }
}
