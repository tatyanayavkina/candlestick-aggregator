package com.bitbucket.tatianayavkina.server.dto

import com.bitbucket.tatianayavkina.server.model.{Candlestick, Ticker}

case class CandlestickResponse(value: Map[Ticker, Iterable[Candlestick]])
