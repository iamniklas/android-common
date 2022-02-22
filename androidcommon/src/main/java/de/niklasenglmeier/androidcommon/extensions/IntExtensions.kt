package de.niklasenglmeier.androidcommon.extensions

object IntExtensions {
    fun Int.flagIsSet(flag: Int) : Boolean {
        return this and flag == flag
    }
}