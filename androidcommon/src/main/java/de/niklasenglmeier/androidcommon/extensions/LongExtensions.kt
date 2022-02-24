package de.niklasenglmeier.androidcommon.extensions

object LongExtensions {
    fun Long.flagIsSet(flag: Long) : Boolean {
        return this and flag == flag
    }
}