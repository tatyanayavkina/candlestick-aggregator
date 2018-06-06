package com.bitbucket.tatianayavkina

import com.bitbucket.tatianayavkina.server.model.{Candlestick, Ticker, UpstreamMessage}
import org.scalatest.{FlatSpec, Matchers}

class CandlestickMergeSpec extends FlatSpec with Matchers {

  val tickerValue = "AAPL"

  val candlestick = Candlestick(Ticker(tickerValue), timestamp = 12345L,
    open = 2.0, high = 4.0, low = 2.0, close = 3.5,
    volume = 1000L)

  "Candlestick" should "throw IllegalArgumentException when merge with different ticker" in {
    val upstreamMessage = UpstreamMessage(12345L, "MSFT", 3.5, 500)

    assertThrows[IllegalArgumentException](
      candlestick.mergeWith(upstreamMessage)
    )
  }

  "Candlestick" should "throw IllegalArgumentException when merge with different timestamp" in {
    val upstreamMessage = UpstreamMessage(12385L, tickerValue, 3.5, 500)

    assertThrows[IllegalArgumentException](
      candlestick.mergeWith(upstreamMessage)
    )
  }

  "Candlestick" should "merge correctly with the same ticker and timestamp" in {
    val upstreamMessage = UpstreamMessage(12345L, tickerValue, 3.2, 500)

    val result = candlestick.mergeWith(upstreamMessage)

    result.ticker should be (Ticker(tickerValue))
    result.timestamp should be (12345L)
    result.open should be (2.0)
    result.high should be (4.0)
    result.low should be (2.0)
    result.close should be (3.2)
    result.volume should be (1500)
  }
}
