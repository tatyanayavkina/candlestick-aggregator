package com.bitbucket.tatianayavkina.server.service

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import akka.io.Tcp._
import com.bitbucket.tatianayavkina.config.ConnectionSettings
import com.bitbucket.tatianayavkina.server.service.UpstreamClientConnector.Reconnect

class UpstreamClient(upstream: ConnectionSettings, aggregator: ActorRef) extends Actor with ActorLogging {

  private val connector = context.actorOf(UpstreamClientConnector.props(new InetSocketAddress(upstream.hostname, upstream.port)), "connector")
  context.watch(connector)

  def receive = {
    case CommandFailed(w: Write) ⇒ // O/S buffer was full
      log.error("write failed")
    case Received(data) ⇒
      aggregator ! UpstreamMessageConverter.getUpstreamMessage(data)
    case _: ConnectionClosed ⇒
      log.info("Connection closed")
      connector ! Reconnect
    case Terminated(_) =>
      log.info("Connector closed")
      context.stop(self)
  }

}

object UpstreamClient {
  def props(upstream: ConnectionSettings, aggregator: ActorRef) =
    Props(new UpstreamClient(upstream, aggregator))
}
