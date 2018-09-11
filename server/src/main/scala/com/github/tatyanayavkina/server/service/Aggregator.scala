package com.github.tatyanayavkina.server.service

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit.MINUTES

import akka.actor.{Actor, ActorLogging, Props}
import com.github.tatyanayavkina.server.model.{Candlestick, UpstreamMessage}
import com.github.tatyanayavkina.server.service.Aggregator.{GetDataForLastMinute, GetDataForLastNMinutes}

import scala.collection.immutable.TreeMap

class Aggregator(keepDataMinutes: Int = 10) extends Actor with ActorLogging {
  private var candlesticks = TreeMap[Long, Map[String, Candlestick]]()

  override def receive: Receive = {
    case msg: UpstreamMessage => processIncomingData(msg)
    case GetDataForLastNMinutes => sender() ! getDataForLastNMinutes(keepDataMinutes)
    case GetDataForLastMinute => sender() ! getDataForLastNMinutes(1)
  }

  private def processIncomingData(msg: UpstreamMessage): Unit = {
    log.info(s"New incoming data $msg")
    val time = msg.ts
    val candles = candlesticks.getOrElse(time, Map[String, Candlestick]())
    val ticker = msg.ticker
    val cs = candles.get(ticker) match {
      case None => Candlestick(ticker, time, msg.price, msg.price, msg.price, msg.price, msg.size)
      case Some(saved) => saved.mergeWith(msg)
    }

    candlesticks += time -> (candles + (ticker -> cs))
  }

  private def getDataForLastNMinutes(n: Int) : Map[String, Iterable[Candlestick]] = {
    val currentMinute = ZonedDateTime.now().truncatedTo(MINUTES).toInstant.toEpochMilli
    (candlesticks - currentMinute)
      .takeRight(n)
      .values
      .flatMap(forMinute => forMinute.values)
      .groupBy(t => t.ticker)
  }
}

object Aggregator {
  case object GetDataForLastNMinutes
  case object GetDataForLastMinute

  def props(keepDataMinutes: Int): Props = Props(new Aggregator(keepDataMinutes))
}
