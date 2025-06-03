package com.excentric.malm.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "server")
data class ServerProperties(
    val port: Int = 8080
)
