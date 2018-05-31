package com.bitbucket.tatianayavkina.server

import akka.actor.ActorSystem
import com.bitbucket.tatianayavkina.server.service.Server.SendDataForLastMinute
import com.bitbucket.tatianayavkina.server.service.{Aggregator, Server, UpstreamClient}
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import com.typesafe.config.ConfigFactory

object ServerApp extends App {

  implicit val system = ActorSystem("candlestick-aggregator")
  val appConfig = ConfigFactory.load()

  val aggregator = system.actorOf(Aggregator.props(appConfig))
  system.actorOf(UpstreamClient.props(appConfig, aggregator))

  val server = system.actorOf(Server.props(appConfig, aggregator))
  val scheduler = QuartzSchedulerExtension(system)
  scheduler.schedule("EveryMinute", server, SendDataForLastMinute)
}
