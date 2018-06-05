package com.bitbucket.tatianayavkina.client

import akka.actor.ActorSystem
import com.bitbucket.tatianayavkina.config.AppConfig

object ClientApp extends App {
  implicit val system = ActorSystem("candlestick-receiver")
  val appConfig = pureconfig.loadConfigOrThrow[AppConfig]

  system.actorOf(Client.props(appConfig.server))
}
