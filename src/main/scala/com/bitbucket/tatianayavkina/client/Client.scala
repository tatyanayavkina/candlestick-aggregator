package com.bitbucket.tatianayavkina.client

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import akka.util.ByteString
import com.typesafe.config.Config

class Client(appConfig: Config) extends Actor with ActorLogging{
  import context.system

  val serverAddress = new InetSocketAddress(appConfig.getString("app.server.host"), appConfig.getInt("app.server.port"))
  IO(Tcp) ! Connect(serverAddress)

  override def receive: Receive = {
    case CommandFailed(_: Connect) =>
      log.error("connect failed")
      context.system.terminate()
    case Connected(remote, local) =>
      val connection = sender()
      connection ! Register(self)
      context become {
        case Received(data) =>
          log.info(data.decodeString(ByteString.UTF_8))
        case _: ConnectionClosed =>
          log.info("connection closed")
          context.system.terminate()
      }
  }
}

object Client {

  def props(appConfig: Config) = Props(new Client(appConfig))
}