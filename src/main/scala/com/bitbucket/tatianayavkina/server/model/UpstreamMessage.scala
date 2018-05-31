package com.bitbucket.tatianayavkina.server.model

case class UpstreamMessage(ts: String,
                           ticker: String,
                           price: Double,
                           size: Int)