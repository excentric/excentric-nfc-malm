package com.excentric.malm.util

import com.excentric.malm.shell.MalmShellCommands
import org.slf4j.LoggerFactory

object SlotArgumentParser {
    private val logger = LoggerFactory.getLogger(MalmShellCommands::class.java)

    fun parseSlotNumbers(slots: String): List<Int> {
        val result = mutableListOf<Int>()

        // First split by commas
        val commaSeparatedParts = slots.trim().split(",")

        for (commaPart in commaSeparatedParts) {
            // Then split each comma-separated part by spaces
            val spaceSeparatedParts = commaPart.trim().split("\\s+".toRegex())

            for (part in spaceSeparatedParts) {
                if (part.isEmpty()) continue

                if (part.contains("-")) {
                    // Handle range format (e.g., "1-5")
                    val range = part.split("-")
                    if (range.size == 2) {
                        try {
                            val start = range[0].toInt()
                            val end = range[1].toInt()
                            if (start <= end) {
                                result.addAll((start..end).toList())
                            } else {
                                logger.warn("Invalid range: $part (start > end)")
                            }
                        } catch (e: NumberFormatException) {
                            logger.warn("Invalid range format: $part")
                        }
                    } else {
                        logger.warn("Invalid range format: $part")
                    }
                } else {
                    // Handle single number
                    try {
                        result.add(part.toInt())
                    } catch (e: NumberFormatException) {
                        logger.warn("Invalid number format: $part")
                    }
                }
            }
        }

        return result
    }
}
