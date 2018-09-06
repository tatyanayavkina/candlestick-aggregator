import Dependencies._

version := "0.1.0-SNAPSHOT"
name := "candlestick-aggregator"
organization := "com.bitbucket.tatianayavkina"
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
  )
  .settings(
    libraryDependencies ++= commonDependencies
  )

lazy val server = project
  .settings(
    name:= "server"
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

