package com.github.tatyanayavkina.server

import java.time.ZonedDateTime

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActorRef, TestKit}
import com.github.tatyanayavkina.server.model.{Candlestick, UpstreamMessage}
import com.github.tatyanayavkina.server.service.Aggregator
import com.github.tatyanayavkina.server.service.Aggregator.{GetDataForLastMinute, GetDataForLastNMinutes}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import scala.concurrent.duration._

class AggregatorSpec() extends TestKit(ActorSystem("MySpec"))
  with ImplicitSender
  with DefaultTimeout
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  val aggregator = TestActorRef(Aggregator.props(10))

  "Aggregator" should {
    "return candlesticks for the last 10 minutes" in {
      within(1.minute) {
        val timeNow = ZonedDateTime.now()
        createMessages(timeNow).foreach(aggregator ! _)

        val future = (aggregator ? GetDataForLastNMinutes).mapTo[Map[String, Iterable[Candlestick]]]
        val candlesticks = Await.result(future, 500.millis)

        candlesticks.size should be (2)
        candlesticks("AAPL").size should be(2)
        candlesticks("MSFT").size should be(1)
      }
    }

    "return candlesticks for the last 1 minute" in {
      within(1.minute) {
        val timeNow = ZonedDateTime.now()
        createMessages(timeNow).foreach(aggregator ! _)
        val message = UpstreamMessage(timeMinusMinutesAsLong(timeNow, 1L), "AAPL", 101.1, 200)
        aggregator ! message

        val future = (aggregator ? GetDataForLastMinute).mapTo[Map[String, Iterable[Candlestick]]]
        val candlesticks = Await.result(future, 500.millis)

        candlesticks.size should be (1)
        candlesticks("AAPL").size should be(1)
      }
    }
  }

  def timeMinusMinutesAsLong(timeNow: ZonedDateTime, minutes: Long): Long = {
    timeNow.minusMinutes(minutes).toInstant.toEpochMilli
  }

  def createMessages(timeNow: ZonedDateTime): Seq[UpstreamMessage] = {
    val data = new ArrayBuffer[UpstreamMessage]()
    data += UpstreamMessage(timeMinusMinutesAsLong(timeNow, 3L), "AAPL", 101.1, 200)
    data += UpstreamMessage(timeMinusMinutesAsLong(timeNow, 3L), "AAPL", 101.2, 100)
    data += UpstreamMessage(timeMinusMinutesAsLong(timeNow, 2L), "AAPL", 101.3, 300)
    data += UpstreamMessage(timeMinusMinutesAsLong(timeNow, 2L), "MSFT", 120.1, 500)
    data += UpstreamMessage(timeMinusMinutesAsLong(timeNow, 2L), "AAPL", 101.0, 700)
    data
  }
}
