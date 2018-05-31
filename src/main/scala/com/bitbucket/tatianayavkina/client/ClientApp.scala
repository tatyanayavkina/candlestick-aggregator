package com.bitbucket.tatianayavkina.client

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

object ClientApp extends App {
  implicit val system = ActorSystem("candlestick-receiver")
  val appConfig = ConfigFactory.load()

  system.actorOf(Client.props(appConfig))
}
