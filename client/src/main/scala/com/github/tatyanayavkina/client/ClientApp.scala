package com.github.tatyanayavkina.client

import akka.actor.ActorSystem

object ClientApp extends App {
  implicit val system = ActorSystem("candlestick-receiver")
  val appConfig = pureconfig.loadConfigOrThrow[AppConfig]

  system.actorOf(Client.props(appConfig))
}
