package com.bitbucket.tatianayavkina.server.model


case class Candlestick(ticker: Ticker,
                       timestamp: Long,
                       open: Double,
                       high: Double,
                       low: Double,
                       close: Double,
                       volume: Long) {

  def mergeWith(incoming: UpstreamMessage) : Candlestick = {
    require(ticker.symbol == incoming.ticker)
    require(timestamp == incoming.ts)

    copy(high = math.max(high, incoming.price),
      low = math.min(low, incoming.price),
      close = incoming.price,
      volume = volume + incoming.size
    )
  }
}
