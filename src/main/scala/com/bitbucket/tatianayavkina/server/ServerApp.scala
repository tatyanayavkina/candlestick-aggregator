package com.bitbucket.tatianayavkina.server

import java.time.LocalDateTime

import akka.actor.ActorSystem
import com.bitbucket.tatianayavkina.config.AppConfig
import com.bitbucket.tatianayavkina.server.service.Server.RequestDataForLastMinute
import com.bitbucket.tatianayavkina.server.service.{Aggregator, Server, UpstreamClient}

import scala.concurrent.duration._

object ServerApp extends App {

  implicit val system = ActorSystem("candlestick-aggregator")
  implicit val executionContext = system.dispatcher

  val appConfig = pureconfig.loadConfigOrThrow[AppConfig]

  val aggregator = system.actorOf(Aggregator.props(appConfig.keepDataMinutes), "aggregator")
  system.actorOf(UpstreamClient.props(appConfig.upstream, aggregator), "upstream")

  val server = system.actorOf(Server.props(appConfig.server, aggregator), "server")
  system.scheduler.schedule((60 - LocalDateTime.now().getSecond).seconds, 1.minute, server, RequestDataForLastMinute)
}
