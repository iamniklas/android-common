package de.niklasenglmeier.androidcommon

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import de.niklasenglmeier.androidcommon.activitydata.AuthenticationData
import de.niklasenglmeier.androidcommon.databinding.ActivityRegisterBinding
import de.niklasenglmeier.androidcommon.extensions.IntExtensions.flagIsSet
import de.niklasenglmeier.androidcommon.extensions.TextInputLayoutExtensions.validateEmailInput
import de.niklasenglmeier.androidcommon.extensions.TextInputLayoutExtensions.validatePasswordInput
import de.niklasenglmeier.androidcommon.extensions.TextInputLayoutExtensions.validatePasswordRepeat
import de.niklasenglmeier.androidcommon.firebase.FirebaseInteractions
import de.niklasenglmeier.androidcommon.firebase.auth.FirebaseAuthHandler
import de.niklasenglmeier.androidcommon.firebase.firestore.FirestoreStandardFetches
import de.niklasenglmeier.androidcommon.firebase.firestore.FirestoreStandardPushes
import de.niklasenglmeier.androidcommon.firebase.remoteconfig.RemoteConfigFetches
import de.niklasenglmeier.androidcommon.models.ResultCode
import java.lang.Exception

internal class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    private lateinit var authenticationData: AuthenticationData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authenticationData = intent.getParcelableExtra(AuthenticationData.IntentKey.PARCELABLE_EXTRA)!!

        supportActionBar!!.title =
            if (authenticationData.showAppNameInSupportActionBar)
                "${authenticationData.appName} Registration"
            else "Registration"

        if(authenticationData.firebaseInteractionMask.flagIsSet(FirebaseInteractions.RemoteConfig)) {
            RemoteConfigFetches.getRemoteConfig(
                applicationContext,
                {
                    if(it.getBoolean(RemoteConfigFetches.EMAIL_REGISTRATION_AVAILABLE)) {
                        binding.buttonRegisterEmail.isEnabled = true
                        binding.textInputLayoutRegisterEmail.isEnabled = true
                        binding.textInputLayoutRegisterPassword.isEnabled = true
                        binding.textInputLayoutRegisterRepeatPassword.isEnabled = true
                        binding.checkBoxRegisterShowPassword.isEnabled = true
                    }
                },
                { remoteConfigFetchError ->
                    finishActivityForResult(ResultCode.ERROR, remoteConfigFetchError)
                }
            )
        }
        else {
            binding.buttonRegisterEmail.isEnabled = true
            binding.textInputLayoutRegisterEmail.isEnabled = true
            binding.textInputLayoutRegisterPassword.isEnabled = true
            binding.textInputLayoutRegisterRepeatPassword.isEnabled = true
            binding.checkBoxRegisterShowPassword.isEnabled = true
        }

        binding.checkBoxRegisterShowPassword.setOnCheckedChangeListener {
                _, checked ->

            if(checked) {
                binding.textInputLayoutRegisterPassword.editText!!.inputType = InputType.TYPE_CLASS_TEXT
                binding.textInputLayoutRegisterRepeatPassword.editText!!.inputType = InputType.TYPE_CLASS_TEXT
            }
            else {
                binding.textInputLayoutRegisterPassword.editText!!.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.textInputLayoutRegisterRepeatPassword.editText!!.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }

        binding.buttonRegisterEmail.setOnClickListener {
            val email = binding.textInputLayoutRegisterEmail.editText!!.editableText.toString().trim()
            val password = binding.textInputLayoutRegisterPassword.editText!!.editableText.toString().trim()
            val passwordRepeat = binding.textInputLayoutRegisterRepeatPassword.editText!!.editableText.toString().trim()

            var error = false

            if(!binding.textInputLayoutRegisterEmail.validateEmailInput()) {
                error = true
            }
            if(!binding.textInputLayoutRegisterPassword.validatePasswordInput()) {
                error = true
            }
            if(!binding.textInputLayoutRegisterRepeatPassword.validatePasswordRepeat(password)) {
                error = true
            }

            if(error) {
                return@setOnClickListener
            }

            if(authenticationData.firebaseInteractionMask.flagIsSet(FirebaseInteractions.Authorization)) {
                FirebaseAuthHandler
                    .Email
                    .performEmailRegistration(
                        email,
                        password,
                        {
                            Toast.makeText(applicationContext, "Please verify your email address by following the instructions we sent you to $email", Toast.LENGTH_LONG).show()
                            Firebase.auth.currentUser!!.sendEmailVerification()
                                .addOnSuccessListener {
                                    Toast.makeText(applicationContext, "Verification Email sent", Toast.LENGTH_LONG).show()
                                }
                                .addOnFailureListener { sendEmailVerificationError ->
                                    Toast.makeText(applicationContext, "Verification Email Error ${sendEmailVerificationError.message.toString()}", Toast.LENGTH_LONG).show()
                                }
                                .addOnCompleteListener {
                                    if(authenticationData.firebaseInteractionMask.flagIsSet(FirebaseInteractions.FirestoreUser)) {
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
                                                        { userDataCreationError ->
                                                            finishActivityForResult(ResultCode.ERROR, userDataCreationError)
                                                        })
                                                },
                                                {
                                                    Toast.makeText(applicationContext, it.message, Toast.LENGTH_LONG).show()
                                                }
                                            )
                                    } else {
                                        finishActivityForResult(ResultCode.SUCCESS)
                                    }
                                }
                        },
                        { emailRegistrationError ->
                            Toast.makeText(applicationContext, emailRegistrationError.message, Toast.LENGTH_LONG).show()
                            finishActivityForResult(ResultCode.ERROR, emailRegistrationError)
                        },
                        { }
                    )
            } else {
                finishActivityForResult(ResultCode.ERROR, Exception("Authentication Flag not set"))
            }
        }

        binding.textViewRegisterLogin.setOnClickListener {
            finishActivityForResult(ResultCode.CANCELED)
        }
    }

    override fun onBackPressed() {
        finishActivityForResult(ResultCode.CANCELED)
    }

    private fun finishActivityForResult(resultCode: Int, error: Exception? = null) {
        val returnIntent = Intent()
        if(error != null) {
            returnIntent.putExtra(LoginActivity.RESULT_EXTRA_ERROR_MESSAGE, error.message.toString())
        }
        setResult(resultCode, returnIntent)
        finish()
    }
}