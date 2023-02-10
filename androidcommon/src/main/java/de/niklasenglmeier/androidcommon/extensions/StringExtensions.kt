package de.niklasenglmeier.androidcommon.extensions

import java.util.*

object StringExtensions {
    fun String.containsUpperCaseLetter(): Boolean {
        for (element in this) {
            if (Character.isUpperCase(element)) {
                return true
            }
        }
        return false
    }

    fun String.containsLowerCaseLetter(): Boolean {
        for (element in this) {
            if (Character.isLowerCase(element)) {
                return true
            }
        }
        return false
    }
}

fun String.humanize(): String {
    return replace(
        String.format(
            "%s|%s|%s",
            "(?<=[A-Z])(?=[A-Z][a-z])",
            "(?<=[^A-Z])(?=[A-Z])",
            "(?<=[A-Za-z])(?=[^A-Za-z])"
        ).toRegex(),
        " "
    ).capitalize(Locale.ROOT)
}