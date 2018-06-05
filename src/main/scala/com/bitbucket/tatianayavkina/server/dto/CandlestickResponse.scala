package com.bitbucket.tatianayavkina.server.dto

import com.bitbucket.tatianayavkina.server.model.{Candlestick, Ticker}
import io.circe.generic.auto._
import io.circe.syntax._

case class CandlestickResponse(value: Map[Ticker, List[Candlestick]])

object CandlestickResponse {

  def toJson(target: CandlestickResponse): String = target.asJson.toString()
}