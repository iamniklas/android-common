package de.niklasenglmeier.androidcommon.firebase.analytics

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

object FirebaseAnalyticsLogger {
    fun logActivityNotImplementedYet(activity: Activity) {
        val bundle = Bundle()
        bundle.putString("activity_name", activity.javaClass.simpleName)
        Firebase.analytics.logEvent("activity_not_implemented_yet", bundle)
    }

    fun logFragmentNotImplementedYet(fragment: Fragment) {
        val bundle = Bundle()
        bundle.putString("fragment_name", fragment.javaClass.simpleName)
        Firebase.analytics.logEvent("fragment_not_implemented_yet", bundle)
    }

    fun logTestActivityAccess(activity: Activity) {
        val bundle = Bundle()
        bundle.putString("activity_name", activity.javaClass.simpleName)
        Firebase.analytics.logEvent("test_activity_access", bundle)
    }
}