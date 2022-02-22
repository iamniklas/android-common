package de.niklasenglmeier.androidcommon.extensions

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