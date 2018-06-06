package com.bitbucket.tatianayavkina.server.service

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import com.bitbucket.tatianayavkina.config.ConnectionSettings
import com.bitbucket.tatianayavkina.server.service.UpstreamClient.Reconnect

class UpstreamClient(upstream: ConnectionSettings, aggregator: ActorRef, retryCount: Int = 10) extends Actor with ActorLogging {
  import context.system

  val upstreamServerAddress = new InetSocketAddress(upstream.hostname, upstream.port)
  var retries = 0

  IO(Tcp) ! Connect(upstreamServerAddress)

  def receive = connecting

  def connecting: Receive = {
    case CommandFailed(_: Connect) ⇒
      log.info("Connection failed")
      self ! Reconnect

    case c @ Connected(remote, local) ⇒
      retries = 0
      log.info("Connected")
      val connection = sender()
      connection ! Register(self)
      context.become(receiveData)

    case Reconnect => doReconnect()
  }

  def receiveData: Receive = {
    case CommandFailed(w: Write) ⇒ // O/S buffer was full
      log.error("write failed")
    case Received(data) ⇒
      aggregator ! UpstreamMessageConverter.getUpstreamMessage(data)
    case _: ConnectionClosed ⇒
      log.info("Connection closed")
      context.become(connecting)
      self ! Reconnect
  }

  def doReconnect(): Unit = {
    if (retries >= retryCount) {
      log.info(s"Retries limit exceeded $retries. Stop context.")
      context.stop(self)
      return
    }

    log.info("Trying to reconnect...")
    retries += 1
    IO(Tcp) ! Connect(upstreamServerAddress)
  }

}

object UpstreamClient {

  case object Reconnect

  def props(upstream: ConnectionSettings, aggregator: ActorRef) =
    Props(new UpstreamClient(upstream, aggregator))
}
