package net.kc5m.sagakka

import akka.actor.typed.ActorRef

import java.util.UUID

sealed trait Message {
  val id: UUID
}

sealed trait Request extends Message
sealed trait SagaRequest extends Request {
  implicit val replyTo: ActorRef[Result]
}
sealed trait Result extends Message
trait OK extends Result
trait NG extends Result

final case class Init(id: UUID) extends Result
final case class A1Req(id: UUID, msg: String)(implicit val replyTo: ActorRef[Result]) extends SagaRequest
final case class A1CompensateReq(id: UUID, msg: String)(implicit val replyTo: ActorRef[Result]) extends SagaRequest
final case class A1Ok(id: UUID, result: String) extends OK
final case class A1CompensateOk(id: UUID, result: String) extends OK
final case class A1Ng(id: UUID, result: Exception) extends NG

final case class A2Req(id: UUID, msg: Int)(implicit val replyTo: ActorRef[Result]) extends SagaRequest
final case class A2Ok(id: UUID, result: Int) extends OK
final case class A2Ng(id: UUID, result: Exception) extends NG

sealed trait Command
case class User(name: String, age: Int)
case class Put(user: User) extends Command
