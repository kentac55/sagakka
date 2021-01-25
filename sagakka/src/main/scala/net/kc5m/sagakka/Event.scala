package net.kc5m.sagakka

import java.util.UUID

import akka.actor.typed.ActorRef

sealed trait Message {
  val id: UUID
}

sealed trait Request extends Message
sealed trait SagaRequest extends Request {
  implicit val replyTo: ActorRef[Orchestrator.Command]
}
sealed trait Result extends Message
trait OK extends Result
trait NG extends Result

final case class A1Req(id: UUID, msg: String)(implicit val replyTo: ActorRef[Orchestrator.Command]) extends SagaRequest
final case class A1CompensateReq(id: UUID, msg: String)(implicit val replyTo: ActorRef[Orchestrator.Command])
    extends SagaRequest
final case class A2Req(id: UUID, msg: Int)(implicit val replyTo: ActorRef[Orchestrator.Command]) extends SagaRequest

sealed trait Command
case class User(name: String, age: Int)
case class Put(user: User) extends Command
