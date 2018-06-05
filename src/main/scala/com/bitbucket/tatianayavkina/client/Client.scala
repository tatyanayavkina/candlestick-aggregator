package com.bitbucket.tatianayavkina.client

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, Props}
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import akka.util.ByteString
import com.bitbucket.tatianayavkina.config.ConnectionSettings

class Client(server: ConnectionSettings) extends Actor with ActorLogging{
  import context.system

  val serverAddress = new InetSocketAddress(server.hostname, server.port)
  IO(Tcp) ! Connect(serverAddress)

  override def receive: Receive = {
    case CommandFailed(_: Connect) =>
      log.error("connect failed")
      context.system.terminate()
    case Connected(remote, local) =>
      val connection = sender()
      connection ! Register(self)
      context.become({
        case Received(data) =>
          log.info(data.decodeString(ByteString.UTF_8))
        case _: ConnectionClosed =>
          log.info("connection closed")
          context.system.terminate()
      })
  }
}

object Client {

  def props(server: ConnectionSettings) = Props(new Client(server))
}