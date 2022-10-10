package de.niklasenglmeier.androidcommon.firebase.remoteconfig

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings

object RemoteConfigFetches {
    const val DEBUG_MODE_ACTIVE = "debug_mode_active"
    const val GOOGLE_LOGIN_AVAILABLE = "google_login_available"
    const val EMAIL_LOGIN_AVAILABLE = "email_login_available"
    const val EMAIL_REGISTRATION_AVAILABLE = "email_registration_available"
    const val REMOTE_CONFIG_MINIMUM_FETCH_INTERVAL = "remote_config_minimum_fetch_interval_in_seconds"

    fun getRemoteConfig(onSuccessListener: OnSuccessListener<FirebaseRemoteConfig>, onFailureListener: OnFailureListener, ignoreFetchInterval: Boolean = false) {
        Firebase.remoteConfig.setConfigSettingsAsync(remoteConfigSettings {
            if(ignoreFetchInterval) {
                minimumFetchIntervalInSeconds = 0
            }
            else {
                minimumFetchIntervalInSeconds = Firebase.remoteConfig.getLong(REMOTE_CONFIG_MINIMUM_FETCH_INTERVAL)
            }
        })

        Firebase.remoteConfig.fetchAndActivate()
            .addOnSuccessListener {
                onSuccessListener.onSuccess(Firebase.remoteConfig)
            }
            .addOnFailureListener {
                onFailureListener.onFailure(it)
            }
    }
}