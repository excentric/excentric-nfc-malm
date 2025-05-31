package com.excentric

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.web.client.RestTemplate
import java.time.Duration.ofSeconds
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    SpringApplication.run(MusicBrainzApplication::class.java, *args)
}

@SpringBootApplication
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
}

@ShellComponent
class ApplicationCommands {

    @ShellMethod(key = ["q"], value = "Exit the application immediately")
    fun quit() {
        exitProcess(0)
    }
}
