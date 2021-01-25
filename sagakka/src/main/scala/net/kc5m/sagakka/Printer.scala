package net.kc5m.sagakka

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object Printer {
  def apply(): Behavior[Int] =
    Behaviors.receive { (ctx, msg) =>
      ctx.log.info("result: {}", msg)
      ctx.spawn(Publisher(), "publisher") ! Put(User("hoge", 10))
      Behaviors.same
    }
}
object Printer2 {
  case class Reply(msg: String)
  case class Ask(put: Put, replyTo: ActorRef[Reply])
  def apply(): Behavior[Ask] =
    Behaviors.receive { (ctx, msg) =>
      ctx.log.info("result: {}", msg.put)
      msg.replyTo ! Reply("ok")
      Behaviors.same
    }
}
