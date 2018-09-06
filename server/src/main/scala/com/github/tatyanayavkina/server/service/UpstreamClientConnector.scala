package com.github.tatyanayavkina.server.service

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.{IO, Tcp}
import akka.io.Tcp.{CommandFailed, Connect, Connected, Register}
import com.github.tatyanayavkina.server.service.UpstreamClientConnector.Reconnect

class UpstreamClientConnector(serverAddress: InetSocketAddress, retryCount: Int = 10) extends Actor with ActorLogging {
  import context.system

  var retries = 0
  IO(Tcp) ! Connect(serverAddress)

  def receive = {
    case CommandFailed(_: Connect) ⇒
      log.info("Connection failed")
      self ! Reconnect

    case c @ Connected(remote, local) ⇒
      retries = 0
      log.info("Connected")
      val connection = sender()
      connection ! Register(context.parent)

    case Reconnect => doReconnect()
  }

  def doReconnect(): Unit = {
    if (retries >= retryCount) {
      log.info(s"Retries limit exceeded $retries. Stop context.")
      context.stop(self)
      return
    }

    log.info("Trying to reconnect...")
    retries += 1
    IO(Tcp) ! Connect(serverAddress)
  }
}

object UpstreamClientConnector {

  case object Reconnect

  def props(serverAddress: InetSocketAddress) =
    Props(new UpstreamClientConnector(serverAddress))
}