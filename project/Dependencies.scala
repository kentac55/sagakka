import sbt._

object Dependencies {
  val akkaV = "2.6.11"
  val akkaHttpV = "10.2.3"
  val circeVersion = "0.12.3"
  val logBackV = "1.2.3"
  val scalatestV = "3.2.2"

  val akka = "com.typesafe.akka" %% "akka-actor-typed" % akkaV
  val akkaCluster = "com.typesafe.akka" %% "akka-cluster-typed" % akkaV
  val akkaHttp = "com.typesafe.akka" %% "akka-http" % akkaHttpV
  val akkaHttpCore = "com.typesafe.akka" %% "akka-http-core" % akkaHttpV
  val akkaHttpSprayJson = "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpV
  val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % akkaV
  val akkaStream = "com.typesafe.akka" %% "akka-stream-typed" % akkaV
  val akkaTestKit = "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaV
  val alpakka = "com.lightbend.akka" %% "akka-stream-alpakka-amqp" % "2.0.2"
  val circeCore = "io.circe" %% "circe-core" % circeVersion
  val circeGeneric = "io.circe" %% "circe-generic" % circeVersion
  val circeParser = "io.circe" %% "circe-parser" % circeVersion
  val logback = "ch.qos.logback" % "logback-classic" % logBackV
  val scalatest = "org.scalatest" %% "scalatest" % scalatestV
}
