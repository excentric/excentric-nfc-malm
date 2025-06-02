package com.excentric

import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ActiveProfiles

@Configuration
@ActiveProfiles("test")
open class MalmTestConfiguration {
    @PostConstruct
    fun init() {
        System.setProperty("spring.shell.history.enabled", "false")
        System.setProperty("spring.shell.log.enabled", "false")
        System.setProperty("org.jline.terminal.dumb", "true")
    }
}
