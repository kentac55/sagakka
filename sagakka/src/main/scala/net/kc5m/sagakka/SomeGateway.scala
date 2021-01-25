package net.kc5m.sagakka

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object SomeGateway {
  trait Command
  case object Increment extends Command
  case object Decrement extends Command
  case object GetValue extends Command
  case object Stop extends Command

  def apply(): Behavior[Command] = {
    def updated(value: Int): Behavior[Command] =
      Behaviors.receive { (ctx, msg) =>
        msg match {
          case Increment => updated(value + 1)
          case Decrement => updated(value - 1)
          case GetValue =>
            val ref = ctx.spawn(Printer(), "result")
            ref ! value
            Behaviors.same
          case Stop =>
            Behaviors.stopped

        }
      }

    updated(0)
  }

}
