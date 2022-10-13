package de.niklasenglmeier.androidcommon.extensions

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import de.niklasenglmeier.androidcommon.R
import de.niklasenglmeier.androidcommon.firebase.firestore.FirestoreStandardFetches
import de.niklasenglmeier.androidcommon.models.standard.UserLevel

fun AppCompatActivity.applyUserModeSettings(context: Context, userLevel: UserLevel? = null) {
    if(userLevel == null) {
        FirestoreStandardFetches.Users.getUserInfo(
            false,
            {
                if(it.userLevel == UserLevel.SuperUser) {
                    supportActionBar!!.setBackgroundDrawable(AppCompatResources.getDrawable(applicationContext, R.color.Blue_500))
                    window.statusBarColor = ContextCompat.getColor(context, R.color.Blue_700)

                    Toast.makeText(applicationContext, "You're In Super User Mode", Toast.LENGTH_LONG).show()
                }
                if(it.userLevel == UserLevel.Admin) {
                    supportActionBar!!.setBackgroundDrawable(AppCompatResources.getDrawable(applicationContext, R.color.Green_500))
                    window.statusBarColor = ContextCompat.getColor(context, R.color.Green_700)

                    Toast.makeText(applicationContext, "You're In Admin Mode", Toast.LENGTH_LONG).show()
                }
            },
            {

            }
        ) {

        }
        return
    }

    if(userLevel == UserLevel.SuperUser) {
        supportActionBar!!.setBackgroundDrawable(AppCompatResources.getDrawable(applicationContext, R.color.Blue_500))
        window.statusBarColor = ContextCompat.getColor(context, R.color.Blue_700)

        Toast.makeText(applicationContext, "You're In Super User Mode", Toast.LENGTH_LONG).show()
    }
    if(userLevel == UserLevel.Admin) {
        supportActionBar!!.setBackgroundDrawable(AppCompatResources.getDrawable(applicationContext, R.color.Green_500))
        window.statusBarColor = ContextCompat.getColor(context, R.color.Green_700)

        Toast.makeText(applicationContext, "You're In Admin Mode", Toast.LENGTH_LONG).show()
    }
}