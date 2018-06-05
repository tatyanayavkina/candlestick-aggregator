package com.bitbucket.tatianayavkina.server.service

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import akka.util.{ByteString, Timeout}
import akka.pattern.ask
import com.bitbucket.tatianayavkina.config.ConnectionSettings
import com.bitbucket.tatianayavkina.server.dto.CandlestickResponse
import com.bitbucket.tatianayavkina.server.model.{Candlestick, Ticker}
import com.bitbucket.tatianayavkina.server.service.Aggregator.{GetDataForLastMinute, GetDataForLastNMinutes}
import com.bitbucket.tatianayavkina.server.service.Server.{RequestDataForLastMinute, SendDataForLastMinute, SendDataForLastNMinutes}

import scala.concurrent.duration._
import scala.util.{Failure, Success}

class Server(server: ConnectionSettings, aggregator: ActorRef) extends Actor with ActorLogging {
  import context.system
  import context.dispatcher

  implicit val timeout = Timeout(1.minutes)
  var clients = Set[ActorRef]()

  IO(Tcp) ! Bind(self, new InetSocketAddress(server.hostname, server.port))

  override def receive = {
    case CommandFailed(b: Bind) =>
      log.error("CommandFailed")
      context.stop(self)
    case _ @ Connected(_, _) => handleNewConnection(sender())
    case RequestDataForLastMinute => requestDataForLastMinute()
    case SendDataForLastMinute(data) => sendDataForLastMinute(data)
    case SendDataForLastNMinutes(connection, data) => sendDataForLastNMinutes(connection, data)
    case _: ConnectionClosed => handleClientDisconnected(sender())
  }

  private def handleNewConnection(connection: ActorRef): Unit = {
    log.info("New client connected. Requesting data for the last 10 minutes")
    val future = (aggregator ? GetDataForLastNMinutes).mapTo[Map[Ticker, List[Candlestick]]]
    future.onComplete {
      case Success(data) =>
        val jsonStr = CandlestickResponse.toJson(CandlestickResponse(data))
        self ! SendDataForLastNMinutes(connection, jsonStr)
      case Failure(e) => log.error(s"Fail to get data for 1 minute: ${e.getMessage}")
    }
  }

  private def requestDataForLastMinute(): Unit = {
    val future = (aggregator ? GetDataForLastMinute).mapTo[Map[Ticker, List[Candlestick]]]
    future.onComplete {
      case Success(data) =>
        val jsonStr = CandlestickResponse.toJson(CandlestickResponse(data))
        self ! SendDataForLastMinute(jsonStr)
      case Failure(e) => log.error(s"Fail to get data for 10 minutes: ${e.getMessage}")
    }
  }

  private def sendDataForLastMinute(data: String): Unit = {
    for (c <- clients) c ! Write(ByteString(data))
  }

  private def sendDataForLastNMinutes(connection: ActorRef, data: String): Unit = {
    log.info(s"Send data for the last 10 minutes to ${connection.path}")
    connection ! Register(self)
    clients += connection
    connection ! Write(ByteString(data))
  }

  private def handleClientDisconnected(connection: ActorRef): Unit = {
    log.info("Client disconnected")
    clients -= connection
  }
}

object Server {
  case object RequestDataForLastMinute
  case class SendDataForLastMinute(data: String)
  case class SendDataForLastNMinutes(connection:ActorRef, data: String)

  def props(server: ConnectionSettings, aggregator: ActorRef) = Props(new Server(server, aggregator))
}


