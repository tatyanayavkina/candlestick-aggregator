package com.bitbucket.tatianayavkina.server.service

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import akka.util.{ByteString, Timeout}
import akka.pattern.ask
import io.circe.generic.auto._
import io.circe.syntax._
import com.bitbucket.tatianayavkina.config.ConnectionSettings
import com.bitbucket.tatianayavkina.server.dto.CandlestickResponse
import com.bitbucket.tatianayavkina.server.model.Candlestick
import com.bitbucket.tatianayavkina.server.service.Aggregator.{GetDataForLastMinute, GetDataForLastNMinutes}
import com.bitbucket.tatianayavkina.server.service.Server.SendDataForLastMinute

import scala.concurrent.duration._
import scala.concurrent.Await

class Server(server: ConnectionSettings, aggregator: ActorRef) extends Actor with ActorLogging {
  import context.system

  implicit val timeout = Timeout(1.minutes)
  var clients = Set[ActorRef]()

  IO(Tcp) ! Bind(self, new InetSocketAddress(server.hostname, server.port))

  override def receive = {
    case CommandFailed(b: Bind) =>
      log.error("CommandFailed")
      context.stop(self)
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
    connection ! Write(ByteString(CandlestickResponse(data).asJson.toString()))
  }

  private def sendDataForLastMinute(): Unit = {
    val future = (aggregator ? GetDataForLastMinute).mapTo[Map[String, Candlestick]]
    val data = Await.result(future, timeout.duration)
    val jsonStr = CandlestickResponse(data).asJson.toString()
    for (c <- clients) c ! Write(ByteString(jsonStr))
  }

  private def handleClientDisconnected(connection: ActorRef): Unit = {
    log.info("Client disconnected")
    clients -= connection
  }
}

object Server {
  case object SendDataForLastMinute

  def props(server: ConnectionSettings, aggregator: ActorRef) = Props(new Server(server, aggregator))
}


