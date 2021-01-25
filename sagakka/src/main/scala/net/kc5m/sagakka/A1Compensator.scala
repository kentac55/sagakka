package net.kc5m.sagakka

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object A1Compensator {
  def apply(): Behavior[A1CompensateReq] =
    Behaviors.receive { (ctx, msg) =>
      ctx.log.info("id: {}", msg.id)
      Main.proxy ! SomeGateway.Decrement
      msg.replyTo ! A1CompensateOk(id = msg.id, result = "ok")
      Behaviors.same
    }
}
