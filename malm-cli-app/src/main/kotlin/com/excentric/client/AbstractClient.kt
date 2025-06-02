package com.excentric.client

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.client.RestTemplate

abstract class AbstractClient() {
    @Value("\${music-album-label-maker.api.user-agent}")
    protected lateinit var userAgent: String

    @Autowired
    protected lateinit var restTemplate: RestTemplate

    protected fun buildRestHttpHeaders(): HttpHeaders {
        return HttpHeaders().apply {
            set("User-Agent", userAgent)
            accept = listOf(APPLICATION_JSON)
        }
    }

    protected fun buildImageHttpHeaders(): HttpHeaders {
        return HttpHeaders().apply {
            set("User-Agent", userAgent)
        }
    }
}
