package net.kc5m.sagakka

import scala.util.Random

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object A1Worker {
  def apply(): Behavior[A1Req] =
    Behaviors.receive { (ctx, msg) =>
      ctx.log.info("id: {}", msg.id)

      Random.nextInt(10) match {
        case x if x < 8 =>
          Main.proxy ! SomeGateway.Increment
          msg.replyTo ! A1Ok(id = msg.id, result = "ok")
        case _ =>
          msg.replyTo ! A1Ng(id = msg.id, new Exception("A1死にました"))
      }

      Behaviors.same
    }
}
