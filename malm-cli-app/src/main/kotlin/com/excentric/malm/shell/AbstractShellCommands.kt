package com.excentric.malm.shell

import com.excentric.malm.errors.MalmException
import com.excentric.malm.util.ConsoleColors.green
import org.jline.terminal.Terminal
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ResourceLoader
import org.springframework.shell.component.SingleItemSelector
import org.springframework.shell.component.support.SelectorItem
import org.springframework.shell.style.TemplateExecutor
import java.awt.Desktop
import java.awt.Desktop.Action.BROWSE
import java.net.URI
import java.util.Locale.getDefault

abstract class AbstractShellCommands {
    @Autowired
    private lateinit var resourceLoader: ResourceLoader

    @Autowired
    lateinit var terminal: Terminal

    @Autowired
    private lateinit var templateExecutor: TemplateExecutor

    abstract val logger: Logger

    companion object {
        const val PROGRESS_BAR_WIDTH = 20
        const val ONE_HUNDRED = 100

    }

    protected fun startProgressBar(totalDownloads: Int) {
        terminal.writer().print("\r[                    ] 0% [0/$totalDownloads]")
        terminal.writer().flush()
    }

    protected fun updateProgressBar(completedDownloads: Int, totalDownloads: Int) {
        val percentage = completedDownloads * ONE_HUNDRED / totalDownloads
        val filledWidth = PROGRESS_BAR_WIDTH * completedDownloads / totalDownloads
        val progressBar = "[" + "=".repeat(filledWidth) + " ".repeat(PROGRESS_BAR_WIDTH - filledWidth) + "]"
        // Clear the line and print updated progress
        terminal.writer().print("\r$progressBar $percentage% [${green(completedDownloads.toString())}/$totalDownloads]")
        terminal.writer().flush()
    }

    protected fun finishProgressBar() {
        terminal.writer().println("\n")
        terminal.writer().flush()
    }

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

    protected fun openUriOnOperatingSystem(uriOrPath: String) {
        val os = System.getProperty("os.name").lowercase(getDefault())
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(BROWSE)) {
            Desktop.getDesktop().browse(URI(uriOrPath));
        } else if (os.indexOf("mac") >= 0) {
            ProcessBuilder("open", uriOrPath).start().waitFor()
        } else {
            logger.warn("Could not open $uriOrPath")
        }
    }
}
