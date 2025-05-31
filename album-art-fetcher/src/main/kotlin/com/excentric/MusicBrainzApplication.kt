package com.excentric

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestTemplate
import java.time.Duration

fun main(args: Array<String>) {
    SpringApplication.run(MusicBrainzApplication::class.java, *args)
}

@SpringBootApplication
open class MusicBrainzApplication {

    @Bean
    open fun restTemplate(): RestTemplate {
        val restTemplate = RestTemplateBuilder()
            .setConnectTimeout(Duration.ofSeconds(5))
            .setReadTimeout(Duration.ofSeconds(5))
            .build()

        // Configure RestTemplate to handle JSON properly
        restTemplate.messageConverters.add(
            0,
            org.springframework.http.converter.json.MappingJackson2HttpMessageConverter()
        )

        return restTemplate
    }
}
