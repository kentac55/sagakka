import Dependencies._
import sbt._

name := "akka-saga"
scalaVersion in ThisBuild := "2.13.4"
organization in ThisBuild := "net.kc5m"
version in ThisBuild := "0.1"

lazy val settings = commonSettings ++ scalafmtSettings ++ wartremoverSettings

lazy val scalafmtSettings = Seq(
  scalafmtOnCompile := true
)

lazy val compilerOptions = Seq(
  "-encoding",
  "UTF-8",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-Xfatal-warnings",
  "-Xlint",
  "-Wunused:imports,params"
)
lazy val wartremoverSettings = Seq(
  wartremoverErrors in (Compile, compile) ++= Warts.all
//  wartremoverErrors in (Compile, compile) ++= ContribWart.All
)

lazy val commonSettings = Seq(
  scalacOptions ++= compilerOptions
)

lazy val commonDeps = Seq(
  akka,
  akkaCluster,
  akkaStream,
  alpakka,
  circeCore,
  circeGeneric,
  circeParser,
  logback,
  akkaTestKit % Test,
  scalatest % Test
)

lazy val global = project
  .in(file("."))
  .settings(settings)
  .aggregate(sagakka)
  .settings(
    name := "global"
  )

lazy val sagakka = project.settings(name := "sagakka", libraryDependencies ++= commonDeps)
