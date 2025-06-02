package com.excentric

import com.excentric.errors.MalmException
import org.slf4j.Logger

abstract class AbstractShellCommands {
    abstract val logger: Logger

    protected fun doSafely(command: () -> Unit) {
        try {
            command()
        } catch (e: MalmException) {
            logger.error(e.message)
        }
    }
}
