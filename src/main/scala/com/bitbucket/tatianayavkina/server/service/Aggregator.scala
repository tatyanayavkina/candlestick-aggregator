package com.bitbucket.tatianayavkina.server.service

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.ISO_INSTANT
import java.time.temporal.ChronoUnit.MINUTES

import akka.actor.{Actor, ActorLogging, Props}
import com.bitbucket.tatianayavkina.server.model.{Candlestick, UpstreamMessage}
import com.bitbucket.tatianayavkina.server.service.Aggregator.{GetDataForLastMinute, GetDataForLastNMinutes}

import scala.collection.immutable.{TreeMap}

class Aggregator(keepDataMinutes: Int = 10) extends Actor with ActorLogging {
  private var candlesticks = TreeMap[String, Map[String, Candlestick]]()

  override def receive: Receive = {
    case msg: UpstreamMessage => processIncomingData(msg)
    case GetDataForLastNMinutes => sender() ! getDataForLastNMinutes(keepDataMinutes)
    case GetDataForLastMinute => sender() ! getDataForLastNMinutes(1)
  }

  private def processIncomingData(msg: UpstreamMessage): Unit = {
    val time = msg.ts
    val candles = candlesticks.getOrElse(time, Map[String, Candlestick]())
    val cs = candles.get(msg.ticker) match {
      case None => Candlestick(msg.ticker, time, msg.price, msg.price, msg.price, msg.price, msg.size)
      case Some(saved) => saved.mergeWith(msg)
    }

    candlesticks += time -> (candles + (msg.ticker -> cs))
  }
  // todo: bug, should return seq of candlestick
  private def getDataForLastNMinutes(n: Int) : Map[String, Candlestick] = {
    val currentMinute = ZonedDateTime.now().truncatedTo(MINUTES).format(ISO_INSTANT)
    (candlesticks - currentMinute)
      .takeRight(n)
      .values
      .flatMap(forMinute => forMinute.values)
      .map(t => t.ticker -> t)
      .toMap
  }
}

object Aggregator {
  case object GetDataForLastNMinutes
  case object GetDataForLastMinute

  def props(keepDataMinutes: Int): Props = Props(new Aggregator(keepDataMinutes))
}
