package de.niklasenglmeier.androidcommon.extensions

import android.util.Patterns
import com.google.android.material.textfield.TextInputLayout
import de.niklasenglmeier.androidcommon.extensions.StringExtensions.containsLowerCaseLetter
import de.niklasenglmeier.androidcommon.extensions.StringExtensions.containsUpperCaseLetter
import java.util.regex.Matcher

object TextInputLayoutExtensions {
    fun TextInputLayout.validateEmailInput(): Boolean {
        val matcher: Matcher = Patterns.EMAIL_ADDRESS.matcher(editText!!.editableText.toString().trim())

        if(editText!!.editableText.toString().isEmpty()) {
            error = "Field cannot be empty"
            return false
        }
        else if(!matcher.matches()) {
            error = "Invalid email"
            return false
        }
        else {
            error = ""
            isErrorEnabled = false
            return true
        }
    }

    fun TextInputLayout.validatePasswordInput(): Boolean {
        val input = editText!!.editableText.toString().trim()
        if(input.isEmpty()) {
            error = "Field cannot be empty"
            return false
        }
        else if(!(input.length > 8 && input.containsUpperCaseLetter() && input.containsLowerCaseLetter())) {
            error = "Password does not match requirements"
            return false
        }
        else {
            error = ""
            isErrorEnabled = false
            return true
        }
    }

    fun TextInputLayout.validatePasswordRepeat(password: String): Boolean {
        return if(editText!!.editableText.toString().trim() != password) {
            error = "Password does not match"
            false
        }
        else {
            error = ""
            isErrorEnabled = false
            true
        }
    }
}