package com.bitbucket.tatianayavkina

import java.nio.ByteBuffer

import akka.util.ByteString
import com.bitbucket.tatianayavkina.server.service.UpstreamMessageConverter
import org.scalatest.{FlatSpec, Matchers}

class UpstreamMessageConverterSpec extends FlatSpec with Matchers {

  "UpstreamMessageConverter" should "properly convert incoming bytestring to UpstreamMessage" in {
      val message = prepareMessage
      val incomingByteString = ByteString(message)
      val upstreamMessage = UpstreamMessageConverter.getUpstreamMessage(incomingByteString)

      upstreamMessage.ticker should be("MSFT")
      upstreamMessage.ts should be(1528363620000L)
      upstreamMessage.price should be(90.9)
      upstreamMessage.size should be(7500)
  }

  def prepareMessage: Array[Byte] = {
    val tickerBytes = "MSFT".getBytes("ascii")
    val contentBuf: ByteBuffer = ByteBuffer.allocate(28)
    contentBuf.putShort(26)
    contentBuf.putLong(1528363623450L)
    contentBuf.putShort(4)
    contentBuf.put(tickerBytes)
    contentBuf.putDouble(90.9)
    contentBuf.putInt(7500)
    contentBuf.array()
  }
}
