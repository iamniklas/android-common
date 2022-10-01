package de.niklasenglmeier.androidcommon.auth

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import de.niklasenglmeier.androidcommon.activitydata.AuthenticationData
import de.niklasenglmeier.androidcommon.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    lateinit var authData: AuthenticationData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authData =
            if (Build.VERSION.SDK_INT >= 33)
                intent.getParcelableExtra(AuthenticationData.IntentKey.PARCELABLE_EXTRA, AuthenticationData::class.java)!!
            else
                intent.getParcelableExtra(AuthenticationData.IntentKey.PARCELABLE_EXTRA)!!

        supportActionBar!!.title =
            if (authData.showAppNameInSupportActionBar)
                "${authData.appName} Login"
            else "Login"
    }

    fun onFragmentFinish(resultCode: Int, error: Exception? = null) {
        val data = Intent()
        if(error != null) {
            data.putExtra("error_name", error.javaClass.toString())
            data.putExtra("error_message", error.message)
        }
        setResult(resultCode, data)
        finish()
    }

    fun toggleView() {
        when(binding.authViewSwitcher.displayedChild) {
            0 -> {
                binding.authViewSwitcher.showNext()
                supportActionBar!!.title =
                    if (authData.showAppNameInSupportActionBar)
                        "${authData.appName} Registration"
                    else "Registration"
            }

            1 -> {
                binding.authViewSwitcher.showPrevious()
                supportActionBar!!.title =
                    if (authData.showAppNameInSupportActionBar)
                        "${authData.appName} Login"
                    else "Login"
            }
        }
    }
}