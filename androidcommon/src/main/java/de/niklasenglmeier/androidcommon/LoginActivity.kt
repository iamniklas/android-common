package de.niklasenglmeier.androidcommon

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import de.niklasenglmeier.androidcommon.activitydata.AuthenticationData
import de.niklasenglmeier.androidcommon.alertdialogs.Dialogs
import de.niklasenglmeier.androidcommon.databinding.ActivityLoginBinding
import de.niklasenglmeier.androidcommon.extensions.LongExtensions.flagIsSet
import de.niklasenglmeier.androidcommon.extensions.TextInputLayoutExtensions.validateEmailInput
import de.niklasenglmeier.androidcommon.extensions.TextInputLayoutExtensions.validatePasswordInput
import de.niklasenglmeier.androidcommon.firebase.FirebaseInteractions
import de.niklasenglmeier.androidcommon.firebase.auth.AuthTypes
import de.niklasenglmeier.androidcommon.firebase.auth.FirebaseAuthHandler
import de.niklasenglmeier.androidcommon.firebase.firestore.FirestoreStandardFetches
import de.niklasenglmeier.androidcommon.firebase.firestore.FirestoreStandardPushes
import de.niklasenglmeier.androidcommon.firebase.remoteconfig.RemoteConfigFetches
import de.niklasenglmeier.androidcommon.models.ResultCode
import java.lang.Exception

class LoginActivity : AppCompatActivity() {

    private val RC_SIGN_IN = 1
    private val RC_REGISTER = 2

    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var binding: ActivityLoginBinding

    private lateinit var authenticationData: AuthenticationData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)

        //FirebaseApp.initializeApp(applicationContext)

        authenticationData = intent.getParcelableExtra(AuthenticationData.IntentKey.PARCELABLE_EXTRA)!!

        supportActionBar!!.title =
            if (authenticationData.showAppNameInSupportActionBar)
                "${authenticationData.appName} Login"
            else "Login"

        if(authenticationData.authIcon != null) {
            binding.imageViewLogin.setImageDrawable(getDrawable(authenticationData.authIcon!!))
        } else {
            binding.imageViewLogin.visibility = View.GONE
        }

        if(authenticationData.flags.flagIsSet(AuthenticationData.Flags.GOOGLE_LOGIN)) {
            val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(authenticationData.googleAuthId!!)
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(this, gso)
        } else {
            binding.buttonGoogleSignIn.visibility = View.GONE
        }

        if(!authenticationData.flags.flagIsSet(AuthenticationData.Flags.EMAIL_LOGIN)) {
            binding.buttonLoginEmail.visibility = View.GONE
        }

        if(authenticationData.flags.flagIsSet(AuthenticationData.Flags.FIREBASE_USE_REMOTE_CONFIG)) {
            RemoteConfigFetches
                .getRemoteConfig(
                    {
                        if(it.getBoolean(RemoteConfigFetches.GOOGLE_LOGIN_AVAILABLE)) {
                            binding.buttonGoogleSignIn.isClickable = true
                        }

                        if(it.getBoolean(RemoteConfigFetches.EMAIL_LOGIN_AVAILABLE)) {
                            binding.buttonLoginEmail.isEnabled = true
                            binding.textInputLayoutLoginPassword.isEnabled = true
                            binding.textInputLayoutLoginEmail.isEnabled = true
                            binding.checkBoxShowPassword.isEnabled = true
                        }
                    },
                    {
                        finishActivityForResult(ResultCode.ERROR)
                    }
                )
        } else {
            binding.buttonLoginEmail.isEnabled = true
            binding.buttonGoogleSignIn.isClickable = true
            binding.textInputLayoutLoginPassword.isEnabled = true
            binding.textInputLayoutLoginEmail.isEnabled = true
            binding.checkBoxShowPassword.isEnabled = true
        }

        binding.buttonGoogleSignIn.setOnClickListener {
            binding.progressBarLogin.isIndeterminate = true
            val intent = googleSignInClient.signInIntent
            startActivityForResult(intent, RC_SIGN_IN)
        }

        binding.checkBoxShowPassword.setOnCheckedChangeListener {
                _, checked ->

            if(checked) {
                binding.textInputLayoutLoginPassword.editText!!.inputType = InputType.TYPE_CLASS_TEXT
            }
            else {
                binding.textInputLayoutLoginPassword.editText!!.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }

        binding.buttonLoginEmail.setOnClickListener {
            val email = binding.textInputLayoutLoginEmail.editText!!.editableText.toString().trim()
            val password = binding.textInputLayoutLoginPassword.editText!!.editableText.toString().trim()

            var error = false

            if(!binding.textInputLayoutLoginEmail.validateEmailInput()) {
                error = true
            }
            if(!binding.textInputLayoutLoginPassword.validatePasswordInput()) {
                error = true
            }

            if(error) {
                return@setOnClickListener
            }

            binding.progressBarLogin.isIndeterminate = true

            FirebaseAuthHandler
                .Email
                .performEmailLogin(
                    email,
                    password,
                    {
                        binding.progressBarLogin.isIndeterminate = false
                        if(!it.user!!.isEmailVerified) {
                            Dialogs
                                .makeEmailNotVerifiedErrorDialog(
                                    this,
                                    { },
                                    {
                                        Firebase
                                            .auth
                                            .currentUser!!
                                            .sendEmailVerification()
                                            .addOnSuccessListener {
                                                Toast.makeText(
                                                    applicationContext,
                                                    "Verification Email sent",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                            .addOnFailureListener { ex ->
                                                Toast.makeText(
                                                    applicationContext,
                                                    "Verification Email Error ${ex.message.toString()}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                    }
                                ).show()
                        } else {
                            if(authenticationData.flags.flagIsSet(AuthenticationData.Flags.FIREBASE_USE_FIRESTORE)) {
                                FirestoreStandardFetches
                                    .Users
                                    .getUserInfo(
                                        true,
                                        {
                                            finishActivityForResult(ResultCode.SUCCESS)
                                        },
                                        {
                                            //User Data does not exist
                                            FirestoreStandardPushes.Users.createNewUserEntry(
                                                {
                                                    finishActivityForResult(ResultCode.SUCCESS)
                                                },
                                                {
                                                    finishActivityForResult(ResultCode.ERROR, it)
                                                })
                                        },
                                        {
                                            finishActivityForResult(ResultCode.ERROR, it)
                                        }
                                    )
                            }
                            else {
                                finishActivityForResult(ResultCode.SUCCESS)
                            }
                        }
                    }
                ) {
                    binding.progressBarLogin.isIndeterminate = false
                    Dialogs.makeLoginErrorDialog(this) { }.show()
                }
        }

        binding.textViewLoginRegister.setOnClickListener {
            val i = Intent(this, RegisterActivity::class.java)
            i.putExtra(AuthenticationData.IntentKey.PARCELABLE_EXTRA, authenticationData)
            startActivityForResult(i, RC_REGISTER)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {
            RC_SIGN_IN -> {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)

                task
                    .addOnSuccessListener {
                        if (task.isSuccessful) {
                            try {
                                val account = task.getResult(ApiException::class.java)!!
                                val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)

                                Firebase.auth.signInWithCredential(credential)
                                    .addOnSuccessListener {
                                        if(authenticationData.flags.flagIsSet(AuthenticationData.Flags.FIREBASE_USE_FIRESTORE)) {
                                            FirestoreStandardFetches
                                                .Users
                                                .getUserInfo(
                                                    true,
                                                    {
                                                        finishActivityForResult(ResultCode.SUCCESS)
                                                    },
                                                    {
                                                        //User Data does not exist
                                                        FirestoreStandardPushes
                                                            .Users
                                                            .createNewUserEntry(
                                                                {
                                                                    finishActivityForResult(ResultCode.SUCCESS)
                                                                },
                                                                {
                                                                    finishActivityForResult(ResultCode.ERROR, it)
                                                                }
                                                            )
                                                    },
                                                    {
                                                        Toast.makeText(applicationContext, it.message, Toast.LENGTH_LONG).show()
                                                    })
                                        } else {
                                            finishActivityForResult(ResultCode.SUCCESS)
                                        }
                                    }
                                    .addOnFailureListener { signInWithCredentialError ->
                                        finishActivityForResult(ResultCode.ERROR, signInWithCredentialError)
                                    }
                                    .addOnCompleteListener {
                                        binding.progressBarLogin.isIndeterminate = false
                                    }

                            } catch (e: ApiException) {
                                binding.progressBarLogin.isIndeterminate = false
                                finishActivityForResult(ResultCode.ERROR, e)
                            }
                        } else {
                            binding.progressBarLogin.isIndeterminate = false
                            finishActivityForResult(ResultCode.ERROR, Exception("Auth-Task not successful"))
                        }
                    }
                    .addOnFailureListener { signedInAccountFromIntentError ->
                        binding.progressBarLogin.isIndeterminate = false
                        finishActivityForResult(ResultCode.ERROR, signedInAccountFromIntentError)
                    }
            }

            RC_REGISTER -> {

            }
        }
    }

    override fun onBackPressed() {
        finishActivityForResult(ResultCode.CANCELED)
    }

    private fun finishActivityForResult(resultCode: Int, error: Exception? = null) {
        val returnIntent = Intent()
        if(error != null) {
            returnIntent.putExtra(RESULT_EXTRA_ERROR_MESSAGE, error.message.toString())
        }
        setResult(resultCode, returnIntent)
        finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    companion object {
        const val RESULT_EXTRA_ERROR_MESSAGE = "error_message"
    }
}