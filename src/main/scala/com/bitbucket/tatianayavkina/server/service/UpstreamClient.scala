package com.bitbucket.tatianayavkina.server.service

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import com.typesafe.config.Config

class UpstreamClient(appConfig: Config, aggregator: ActorRef) extends Actor with ActorLogging {
  import context.system

  val upstreamServerAddress = new InetSocketAddress(appConfig.getString("app.upstream.hostname"),
                              appConfig.getInt("app.upstream.port"))

  IO(Tcp) ! Connect(upstreamServerAddress)

  def receive = {
    case CommandFailed(_: Connect) ⇒
      aggregator ! "connect failed"
      context stop self

    case c @ Connected(remote, local) ⇒
      val connection = sender()
      connection ! Register(self)
      context become {
        case CommandFailed(w: Write) ⇒ // O/S buffer was full
          log.error("write failed")
        case Received(data) ⇒
          aggregator ! UpstreamMessageConverter.getUpstreamMessage(data)
        case "close" ⇒
          connection ! Close
        case _: ConnectionClosed ⇒
          log.info("connection closed")
          context stop self
      }
  }
}

object UpstreamClient {
  def props(appConfig: Config, aggregator: ActorRef) =
    Props(new UpstreamClient(appConfig, aggregator))
}
