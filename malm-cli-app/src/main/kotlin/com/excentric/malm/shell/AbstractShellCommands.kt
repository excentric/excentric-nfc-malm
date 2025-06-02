package com.excentric.malm.shell

import com.excentric.malm.errors.MalmException
import org.jline.terminal.Terminal
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ResourceLoader
import org.springframework.shell.component.SingleItemSelector
import org.springframework.shell.component.support.SelectorItem
import org.springframework.shell.style.TemplateExecutor

abstract class AbstractShellCommands {
    @Autowired
    private lateinit var resourceLoader: ResourceLoader

    @Autowired
    private lateinit var terminal: Terminal

    @Autowired
    private lateinit var templateExecutor: TemplateExecutor

    abstract val logger: Logger

    protected fun createSingleItemSelector(
        selectorItems: MutableList<SelectorItem<String>>,
        message: String
    ): SingleItemSelector<String, SelectorItem<String>> {
        return SingleItemSelector(terminal, selectorItems, message, null).apply {
            setResourceLoader(resourceLoader)
            this.templateExecutor = this@AbstractShellCommands.templateExecutor
            setMaxItems(20)
        }
    }

    protected fun doSafely(command: () -> Unit) {
        try {
            command()
        } catch (e: MalmException) {
            logger.error(e.message)
        }
    }
}
