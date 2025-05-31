package com.excentric.util

object ConsoleColors {
    private const val RESET = "\u001B[0m"
    private const val GREEN = "\u001B[32m"
    private const val RED = "\u001B[31m"

    fun greenOrRed(text: Any?): String {
        if (text == null || text.toString().isEmpty()) {
            return "${RED}Not Found$RESET"
        }
        return "$GREEN$text$RESET"
    }
}
