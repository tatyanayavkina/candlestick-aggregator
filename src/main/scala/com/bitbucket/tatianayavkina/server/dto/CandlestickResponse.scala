package com.bitbucket.tatianayavkina.server.dto

import com.bitbucket.tatianayavkina.server.model.Candlestick

case class CandlestickResponse(value: Map[String, Candlestick])
