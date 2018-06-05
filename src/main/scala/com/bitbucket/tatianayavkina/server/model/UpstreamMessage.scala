package com.bitbucket.tatianayavkina.server.model

case class UpstreamMessage(ts: Long,
                           ticker: String,
                           price: Double,
                           size: Int)