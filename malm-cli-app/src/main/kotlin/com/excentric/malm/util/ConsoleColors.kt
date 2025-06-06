package com.excentric.malm.util

object ConsoleColors {
    private const val RESET = "\u001B[0m"
    private const val RED = "\u001B[31m"
    private const val GREEN = "\u001B[32m"
    private const val YELLOW = "\u001B[33m"

    fun greenOrRed(text: Any?): String {
        if (text == null || text.toString().isEmpty()) {
            return "${RED}Unknown$RESET"
        }
        return "$GREEN$text$RESET"
    }

    fun green(text: String): String {
        return "$GREEN$text$RESET"
    }

    fun red(text: String): String {
        return "$RED${text}$RESET"
    }

    fun yellow(text: String): String {
        return "$YELLOW${text}$RESET"
    }
}
