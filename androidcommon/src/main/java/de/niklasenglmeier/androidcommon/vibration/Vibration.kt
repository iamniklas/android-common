package de.niklasenglmeier.androidcommon.vibration

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

object Vibration {

    @Throws(java.lang.IllegalArgumentException::class)
    fun vibrate(application: Application, duration: Long) {
        if(duration < 1L) {
            throw java.lang.IllegalArgumentException("Duration must be at least 1ms long")
        }

        val v = application.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            v.vibrate(duration)
        }
    }
}