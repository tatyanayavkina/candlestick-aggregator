import sbt._

object Dependencies {
  // Versions
  lazy val logbackVersion = "1.2.3"
  lazy val scalaLoggingVersion = "3.9.0"
  lazy val akkaVersion = "2.5.12"
  lazy val commonsCollectionsVersion = "4.1"
  lazy val gsonVersion = "2.8.5"
  lazy val quartzVersion = "1.6.1-akka-2.5.x"

  // Libraries
  val logbackClassic = "ch.qos.logback" % "logback-classic" % logbackVersion
  val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion
  val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaVersion
  val gson = "com.google.code.gson" % "gson" % gsonVersion
  val quartz = "com.enragedginger" %% "akka-quartz-scheduler" % quartzVersion
}