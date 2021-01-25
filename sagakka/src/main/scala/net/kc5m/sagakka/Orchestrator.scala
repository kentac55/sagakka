package net.kc5m.sagakka

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

object Orchestrator {
  def apply(): Behavior[Result] =
    Behaviors.receive { (ctx, msg) =>
      ctx.log.info("received: {}", msg.id)
      implicit val self: ActorRef[Result] = ctx.self

      msg match {
        case Init(_) =>
          val ref = ctx.spawn(A1Worker(), "A1")
          ref ! A1Req(id = msg.id, msg = "plz")
          ctx.spawn(Subscriber(), "subscriber") ! Subscriber.Exec
        case A1Ok(_, _) =>
          val ref = ctx.spawn(A2Worker(), "A2")
          ref ! A2Req(msg.id, 1)
        case A1Ng(_, _) =>
          ctx.log.info("A1 fail!")
          Main.proxy ! SomeGateway.GetValue
        case A1CompensateOk(_, _) =>
          ctx.log.info("A1 compensated!")
          Main.proxy ! SomeGateway.GetValue
        case A2Ok(_, _) =>
          ctx.log.info("success!")
          Main.proxy ! SomeGateway.GetValue
        case A2Ng(_, _) =>
          ctx.log.info("A2 fail!")
          val ref = ctx.spawn(A1Compensator(), "A1Comp")
          ref ! A1CompensateReq(id = msg.id, msg = "plz")
      }
      Behaviors.same
    }
}
