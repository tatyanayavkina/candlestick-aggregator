package com.bitbucket.tatianayavkina.server.service

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import akka.util.{ByteString, Timeout}
import akka.pattern.ask
import com.bitbucket.tatianayavkina.server.model.Candlestick
import com.bitbucket.tatianayavkina.server.service.Aggregator.{GetDataForLastMinute, GetDataForLastNMinutes}
import com.bitbucket.tatianayavkina.server.service.Server.SendDataForLastMinute
import com.google.gson.Gson
import com.typesafe.config.Config

import scala.concurrent.duration._
import scala.concurrent.Await

class Server(appConfig: Config, aggregator: ActorRef) extends Actor with ActorLogging {
  import context.system

  implicit val timeout = Timeout(1.minutes)
  val parser = new Gson
  var clients = Set[ActorRef]()

  IO(Tcp) ! Bind(self, new InetSocketAddress(appConfig.getString("app.server.host"), appConfig.getInt("app.server.port")))

  override def receive = {
    case CommandFailed(b: Bind) =>
      log.error("CommandFailed")
      context stop self
    case _ @ Connected(_, _) => handleNewConnection(sender())
    case SendDataForLastMinute => sendDataForLastMinute()
    case _: ConnectionClosed => handleClientDisconnected(sender())
  }

  private def handleNewConnection(connection: ActorRef): Unit = {
    connection ! Register(self)
    clients += connection
    log.info("New client connected. Sending data for the last 10 minutes")
    val future = (aggregator ? GetDataForLastNMinutes).mapTo[Map[String, Candlestick]]
    val data = Await.result(future, timeout.duration)
    connection ! Write(ByteString(parser toJson data))
  }

  private def sendDataForLastMinute(): Unit = {
    val future = (aggregator ? GetDataForLastMinute).mapTo[Map[String, Candlestick]]
    val data = Await.result(future, timeout.duration)
    val jsonStr = parser toJson data
    for (c <- clients) c ! Write(ByteString(jsonStr))
  }

  private def handleClientDisconnected(connection: ActorRef): Unit = {
    log.info("Client disconnected")
    clients -= connection
  }
}

object Server {
  case object SendDataForLastMinute

  def props(appConfig: Config, aggregator: ActorRef) = Props(new Server(appConfig, aggregator))
}


