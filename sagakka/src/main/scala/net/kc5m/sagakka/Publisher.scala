package net.kc5m.sagakka

import scala.concurrent.duration.DurationInt
import scala.concurrent.Future

import akka.Done
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.alpakka.amqp.scaladsl.AmqpFlow
import akka.stream.alpakka.amqp.{
  AmqpLocalConnectionProvider,
  AmqpWriteSettings,
  QueueDeclaration,
  WriteMessage,
  WriteResult
}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.ByteString
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

object Publisher {
  def apply(): Behavior[Put] =
    Behaviors.receive { (ctx, msg) =>
      ctx.log.info("Exec!: {}", msg)
      import Main.system
      val connectionProvider = AmqpLocalConnectionProvider
      val queueName = "sagakka"
      val queueDec = QueueDeclaration(queueName)
      val settings = AmqpWriteSettings(connectionProvider)
        .withRoutingKey(queueName)
        .withDeclaration(queueDec)
        .withBufferSize(10)
        .withConfirmationTimeout((200.millis))

      val amqpFlow: Flow[WriteMessage, WriteResult, Future[Done]] = AmqpFlow.withConfirm(settings)

      val input = msg.asJson.noSpaces
      val result: Future[Seq[WriteResult]] =
        Source(Vector(input))
          .map(msg => WriteMessage(ByteString(msg)))
          .via(amqpFlow)
          .runWith(Sink.seq)
      Behaviors.same
    }
}
