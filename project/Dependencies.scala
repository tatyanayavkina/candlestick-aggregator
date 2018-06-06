import sbt._

object Dependencies {
  // Versions
  lazy val logbackVersion = "1.2.3"
  lazy val scalaLoggingVersion = "3.9.0"
  lazy val akkaVersion = "2.5.12"
  lazy val commonsCollectionsVersion = "4.1"
  lazy val circeVersion = "0.10.0-M1"
  lazy val pureConfigVersion = "0.9.1"
  lazy val scalaTestVersion = "3.0.5"

  // Libraries
  val logbackClassic = "ch.qos.logback" %% "logback-classic" % logbackVersion
  val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion
  val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaVersion
  val circe = "io.circe" %% "circe-core" % circeVersion
  val circeGeneric = "io.circe" %% "circe-generic" % circeVersion
  val pureConfig = "com.github.pureconfig" %% "pureconfig" % pureConfigVersion
  val scalaTest = "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
}