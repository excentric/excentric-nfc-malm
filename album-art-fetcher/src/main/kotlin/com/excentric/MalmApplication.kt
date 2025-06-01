package com.excentric

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestTemplate
import java.time.Duration.ofSeconds

fun main(args: Array<String>) {
    SpringApplication.run(MusicBrainzApplication::class.java, *args)
}

@SpringBootApplication
@ConfigurationPropertiesScan("com.excentric.config")
open class MusicBrainzApplication {
    companion object {
        private val FIVE_SECONDS = ofSeconds(5L)
    }

    @Bean
    open fun restTemplate(): RestTemplate {
        return RestTemplateBuilder()
            .setConnectTimeout(FIVE_SECONDS)
            .setReadTimeout(FIVE_SECONDS)
            .build().apply {
                messageConverters.add(0, MappingJackson2HttpMessageConverter())
            }
    }

    @Bean
    open fun objectMapper(): ObjectMapper {
        return jacksonObjectMapper()
    }
}
