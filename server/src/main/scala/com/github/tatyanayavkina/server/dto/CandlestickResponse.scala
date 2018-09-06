package com.github.tatyanayavkina.server.dto

import com.github.tatyanayavkina.server.model.{Candlestick, Ticker}
import io.circe.generic.auto._
import io.circe.syntax._

case class CandlestickResponse(value: Seq[(Ticker, Iterable[Candlestick])])

object CandlestickResponse {

  implicit class CandlestickResponseExt(response: CandlestickResponse) {
    def toJson: String = response.asJson.toString()
  }

  def apply(source: Map[Ticker, Iterable[Candlestick]]): CandlestickResponse = CandlestickResponse(
    source.map { case (ticker, canclesticks) => (ticker, canclesticks) }
  )
}