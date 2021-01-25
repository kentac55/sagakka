package net.kc5m.sagakka

import java.util.UUID

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}

object Orchestrator {
  sealed trait Command
  case object Init extends Command
  final case class A1Ok(id: UUID, result: String) extends Command
  final case class A1Ng(id: UUID, result: Exception) extends Command
  final case class A1CompensateOk(id: UUID, result: String) extends Command
  final case class A2Ok(id: UUID, result: Int) extends Command
  final case class A2Ng(id: UUID, result: Exception) extends Command
  case object Clear extends Command

  sealed trait Event
  final case class Inited(id: UUID, nextReq: A1Req) extends Event
  final case class A1Succeeded(id: UUID, result: String, nextReq: A2Req) extends Event
  final case class A1Failed(id: UUID, result: Exception) extends Event
  final case class A1Compensated(id: UUID, result: String) extends Event
  final case class A2Succeeded(id: UUID, result: Int) extends Event
  final case class A2Failed(id: UUID, result: Exception, compensateReq: A1CompensateReq) extends Event
  case object Cleared extends Event

  sealed trait Result
  case object Success extends Result
  case object Failure extends Result
  case object Compensated extends Result
  case object NotYet extends Result
  case object Skip extends Result

  final case class State(
      a1: Result = NotYet,
      a1Req: Option[A1Req] = None,
      a1CompReq: Option[A1CompensateReq] = None,
      a2: Result = NotYet,
      a2Req: Option[A2Req] = None
  )

  def apply(id: UUID): Behavior[Command] =
    Behaviors.setup { ctx =>
      implicit val self: ActorRef[Command] = ctx.self
      EventSourcedBehavior[Command, Event, State](
        persistenceId = PersistenceId.ofUniqueId(id.toString),
        emptyState = State(),
        commandHandler = { (state, cmd) =>
          cmd match {
            case Init =>
              Effect
                .persist(Inited(id, A1Req(id, "I am A1Req!")))
                .thenRun { newState =>
                  newState.a1Req.foreach(ctx.spawn(A1Worker(), "A1") ! _)
                  ctx.spawn(Subscriber(), "subscriber") ! Subscriber.Exec
                }
            case A1Ok(x, y) =>
              Effect.persist(A1Succeeded(x, y, A2Req(id, 1))).thenRun { newState =>
                newState.a2Req.foreach(ctx.spawn(A2Worker(), "A2") ! _)
              }
            case A1Ng(x, y) =>
              Effect.persist(A1Failed(x, y)).thenRun { newState =>
                ctx.log.info("A1 fail!")
                Main.proxy ! SomeGateway.GetValue
              }
            case A1CompensateOk(x, y) =>
              Effect.persist(A1Compensated(x, y)).thenRun { newState =>
                Main.proxy ! SomeGateway.GetValue
              }
            case A2Ok(x, y) =>
              Effect.persist(A2Succeeded(x, y)).thenRun { newState =>
                Main.proxy ! SomeGateway.GetValue
              }
            case A2Ng(x, y) =>
              Effect.persist(A2Failed(x, y, A1CompensateReq(id, "plz"))).thenRun { newState =>
                ctx.log.info("A2 fail!")
                newState.a1CompReq.foreach(ctx.spawn(A1Compensator(), "A1Comp") ! _)
              }
            case Clear => Effect.persist(Cleared)
          }
        },
        eventHandler = { (state, event) =>
          event match {
            case Cleared              => State()
            case Inited(_, x)         => state.copy(a1Req = Some(x))
            case A1Succeeded(_, _, x) => state.copy(Success, a2Req = Some(x))
            case A1Failed(_, _)       => state.copy(Failure, a2 = Skip)
            case A1Compensated(_, _)  => state.copy(Compensated)
            case A2Succeeded(_, _)    => state.copy(a2 = Success)
            case A2Failed(_, _, x)    => state.copy(a2 = Failure, a1CompReq = Some(x))
          }
        }
      )
    }
}
