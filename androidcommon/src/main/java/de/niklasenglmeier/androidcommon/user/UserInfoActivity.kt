package de.niklasenglmeier.androidcommon.user

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import de.niklasenglmeier.androidcommon.R

class UserInfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)
    }
}