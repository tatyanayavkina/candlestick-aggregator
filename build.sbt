import Dependencies._

version := "0.1.0"
name := "candlestick-aggregator"
organization := "com.github.tatyanayavkina"
scalaVersion := "2.12.6"

lazy val root  = project
  .in(file("."))
  .aggregate(
    server,
    client
  )

lazy val client = project
  .settings(
    name := "client",
    mainClass in Compile := Some(" com.github.tatyanayavkina.client.ClientApp")
  )
  .settings(
    libraryDependencies ++= commonDependencies
  )

lazy val server = project
  .settings(
    name:= "server",
    mainClass in Compile := Some(" com.github.tatyanayavkina.server.ServerApp")
  )
  .settings(
    libraryDependencies ++= commonDependencies ++ serverDependencies
  )

lazy val commonDependencies = Seq(
  logbackClassic,
  scalaLogging,
  akkaActor,
  pureConfig,
)

lazy val serverDependencies = Seq(
  circe,
  circeGeneric,
  scalaTest,
  akkaTest
)
