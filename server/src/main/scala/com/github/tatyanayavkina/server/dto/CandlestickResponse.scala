package com.github.tatyanayavkina.server.dto

import com.github.tatyanayavkina.server.model.Candlestick
import io.circe.generic.auto._
import io.circe.syntax._

case class CandlestickResponse(value: Map[String, Iterable[Candlestick]]) {
  def toJson: String = value.asJson.toString()
}
