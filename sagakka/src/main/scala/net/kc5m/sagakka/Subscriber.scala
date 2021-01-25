package net.kc5m.sagakka

import scala.concurrent.duration.DurationInt

import akka.NotUsed
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.alpakka.amqp.scaladsl.AmqpSource
import akka.stream.alpakka.amqp.{AmqpLocalConnectionProvider, NamedQueueSourceSettings, QueueDeclaration, ReadResult}
import akka.stream.Attributes
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.typed.scaladsl.ActorFlow
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

object Subscriber {

  trait Command
  case object Exec extends Command

  def apply(): Behavior[Command] =
    Behaviors.receive { (ctx, _) =>
      ctx.log.info("ready to subscribe")
      import Main.system

      val connectionProvider = AmqpLocalConnectionProvider
      val queueName = "sagakka"
      val queueDec = QueueDeclaration(queueName)

      val amqpSource: Source[ReadResult, NotUsed] =
        AmqpSource.atMostOnceSource(
          NamedQueueSourceSettings(connectionProvider, queueName)
            .withDeclaration(queueDec)
            .withAckRequired(true),
          bufferSize = 10
        )

      val ref = ctx.spawn(Printer2(), "printer2")

      implicit val timeout: akka.util.Timeout = 1.second

      val flow: Flow[Put, Printer2.Reply, NotUsed] = ActorFlow.ask(ref)(Printer2.Ask.apply)

      amqpSource
        .take(10)
        .log("debug", x => x.bytes.utf8String)
        .map(x => decode[Put](x.bytes.utf8String))
        .collect({ case Right(x) => x })
        .log("debug2", x => x)
        .via(flow)
        .map(_.msg)
        .addAttributes(
          Attributes.logLevels(
            onElement = Attributes.LogLevels.Info,
            onFinish = Attributes.LogLevels.Info,
            onFailure = Attributes.LogLevels.Error
          )
        )
        .runWith(Sink.seq)

      Behaviors.same
    }
}
