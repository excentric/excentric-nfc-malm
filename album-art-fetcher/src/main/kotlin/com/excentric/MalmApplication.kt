package com.excentric

import com.excentric.client.LoggingInterceptor
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.jline.utils.AttributedString
import org.jline.utils.AttributedStyle
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.client.BufferingClientHttpRequestFactory
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.shell.command.annotation.CommandScan
import org.springframework.shell.jline.PromptProvider
import org.springframework.web.client.RestTemplate
import java.time.Duration.ofSeconds


fun main(args: Array<String>) {
    SpringApplication.run(MusicBrainzApplication::class.java, *args)
}

@SpringBootApplication
@CommandScan
@ConfigurationPropertiesScan("com.excentric.config")
open class MusicBrainzApplication {
    companion object {
        private val FIVE_SECONDS = ofSeconds(5L)
    }

    @Bean
    open fun restTemplate(loggingInterceptor: LoggingInterceptor): RestTemplate {
        return RestTemplateBuilder()
            .setConnectTimeout(FIVE_SECONDS)
            .setReadTimeout(FIVE_SECONDS)
            .build().apply {
                requestFactory = BufferingClientHttpRequestFactory(SimpleClientHttpRequestFactory())
                messageConverters.add(0, MappingJackson2HttpMessageConverter())
                interceptors.add(loggingInterceptor)
            }
    }

    @Bean
    open fun myPromptProvider(): PromptProvider {
        return PromptProvider { AttributedString("malm:>", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW)) }
    }

    @Bean
    @Primary
    open fun objectMapper(): ObjectMapper? =
        jacksonObjectMapper()
}
