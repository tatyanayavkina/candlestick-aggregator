package com.bitbucket.tatianayavkina.config

case class ConnectionSettings(hostname: String, port: Int)

case class AppConfig(upstream: ConnectionSettings,
                     server: ConnectionSettings,
                     keepDataMinutes: Int)

