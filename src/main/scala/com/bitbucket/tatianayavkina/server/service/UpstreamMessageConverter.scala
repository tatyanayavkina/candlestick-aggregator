package com.bitbucket.tatianayavkina.server.service

import java.time.temporal.ChronoUnit.MINUTES
import java.time.{Instant, ZoneId, ZonedDateTime}

import akka.util.ByteString
import com.bitbucket.tatianayavkina.server.model.UpstreamMessage

object UpstreamMessageConverter {

  def getUpstreamMessage(data: ByteString): UpstreamMessage = {
    val buf = data.toByteBuffer
    buf.getChar.toInt
    val timestamp = parseTimestamp(buf.getLong)
    val tickerNameLength = buf.getChar.toInt
    val ticker = (for(_ <- 1 to tickerNameLength) yield buf.get.toChar).mkString
    val price = buf.getDouble
    val size = buf.getInt

    UpstreamMessage(timestamp, ticker, price, size)
  }

  private def parseTimestamp(timestamp: Long): Long = ZonedDateTime.ofInstant(
    Instant.ofEpochMilli(timestamp),
    ZoneId.systemDefault())
    .truncatedTo(MINUTES)
    .toInstant
    .toEpochMilli
}
