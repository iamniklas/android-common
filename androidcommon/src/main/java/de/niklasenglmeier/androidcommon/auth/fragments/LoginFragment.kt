package de.niklasenglmeier.androidcommon.auth.fragments

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import de.niklasenglmeier.androidcommon.activitydata.AuthenticationData
import de.niklasenglmeier.androidcommon.alertdialogs.Dialogs
import de.niklasenglmeier.androidcommon.auth.AuthActivity
import de.niklasenglmeier.androidcommon.databinding.FragmentLoginBinding
import de.niklasenglmeier.androidcommon.extensions.LongExtensions.flagIsSet
import de.niklasenglmeier.androidcommon.extensions.TextInputLayoutExtensions.validateEmailInput
import de.niklasenglmeier.androidcommon.extensions.TextInputLayoutExtensions.validatePasswordInput
import de.niklasenglmeier.androidcommon.firebase.auth.FirebaseAuthHandler
import de.niklasenglmeier.androidcommon.firebase.firestore.FirestoreStandardFetches
import de.niklasenglmeier.androidcommon.firebase.firestore.FirestoreStandardPushes
import de.niklasenglmeier.androidcommon.firebase.remoteconfig.RemoteConfigFetches
import de.niklasenglmeier.androidcommon.models.standard.LoginMethod

class LoginFragment : Fragment() {

    private val RC_SIGN_IN = 1

    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var binding: FragmentLoginBinding

    private lateinit var authenticationData: AuthenticationData

    private lateinit var hostActivity: AuthActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(layoutInflater)

        hostActivity = requireActivity() as AuthActivity

        authenticationData = hostActivity.authData

        if(authenticationData.flags.flagIsSet(AuthenticationData.Flags.LOGIN_OPTIONAL)) {
            binding.textViewLoginHint.text = "Login by one of the following methods to use all features of ${authenticationData.appName}"
        } else {
            binding.textViewLoginHint.text = "To continue using ${authenticationData.appName}, please login by one of the available options"
        }

        if(authenticationData.authIcon != null) {
            binding.imageViewLogin.setImageDrawable(ContextCompat.getDrawable(requireContext(), authenticationData.authIcon!!))
        } else {
            binding.imageViewLogin.visibility = View.GONE
        }

        if(authenticationData.flags.flagIsSet(AuthenticationData.Flags.GOOGLE_LOGIN)) {
            val gso =
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(authenticationData.googleAuthId!!)
                    .requestEmail()
                    .build()

            googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
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
                        hostActivity.onFragmentFinish(AuthActivity.Result.ERROR_FETCH_REMOTE_CONFIG, it)
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
                                    requireActivity(),
                                    { },
                                    {
                                        Firebase
                                            .auth
                                            .currentUser!!
                                            .sendEmailVerification()
                                            .addOnSuccessListener {
                                                Toast.makeText(
                                                    requireContext(),
                                                    "Verification Email sent",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                            .addOnFailureListener { ex ->
                                                hostActivity.onFragmentFinish(AuthActivity.Result.ERROR_ACCOUNT_NOT_VERIFIED, ex)
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
                                            hostActivity.onFragmentFinish(AuthActivity.Result.EMAIL_LOGIN_SUCCESS)
                                        },
                                        {
                                            //User Data does not exist
                                            FirestoreStandardPushes.Users.createNewUserEntry(
                                                LoginMethod.Email,
                                                null,
                                                null,
                                                {
                                                    hostActivity.onFragmentFinish(AuthActivity.Result.EMAIL_LOGIN_SUCCESS)
                                                },
                                                { userError ->
                                                    hostActivity.onFragmentFinish(AuthActivity.Result.ERROR_LOGIN_CREATE_FIRESTORE_DATA, userError)
                                                })
                                        },
                                        { userInfoError ->
                                            hostActivity.onFragmentFinish(AuthActivity.Result.ERROR_USER_DATA_FETCH_OR_INVALID, userInfoError)
                                        }
                                    )
                            }
                            else {
                                hostActivity.onFragmentFinish(AuthActivity.Result.EMAIL_LOGIN_SUCCESS)
                            }
                        }
                    }
                ) {
                    if(it::class == FirebaseAuthInvalidUserException::class) {
                        hostActivity.onFragmentFinish(AuthActivity.Result.ERROR_LOGIN_ACCOUNT_DISABLED, it)
                        return@performEmailLogin
                    }
                    binding.progressBarLogin.isIndeterminate = false
                    Dialogs.makeLoginErrorDialog(requireActivity()) { }.show()
                }
        }

        binding.textViewLoginRegister.setOnClickListener {
            hostActivity.toggleView()
        }

        return binding.root
    }

    @Deprecated("Deprecated in Java")
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
                                                        hostActivity.onFragmentFinish(AuthActivity.Result.GOOGLE_LOGIN_SUCCESS)
                                                    },
                                                    {
                                                        //User Data does not exist
                                                        FirestoreStandardPushes
                                                            .Users
                                                            .createNewUserEntry(
                                                                LoginMethod.Google,
                                                                null,
                                                                null,
                                                                {
                                                                    hostActivity.onFragmentFinish(AuthActivity.Result.GOOGLE_LOGIN_SUCCESS)
                                                                },
                                                                {
                                                                    hostActivity.onFragmentFinish(AuthActivity.Result.ERROR_LOGIN_CREATE_FIRESTORE_DATA, it)
                                                                }
                                                            )
                                                    },
                                                    {
                                                        hostActivity.onFragmentFinish(AuthActivity.Result.ERROR_USER_DATA_FETCH_OR_INVALID, it)
                                                    })
                                        } else {
                                            hostActivity.onFragmentFinish(AuthActivity.Result.GOOGLE_LOGIN_SUCCESS)
                                        }
                                    }
                                    .addOnFailureListener { signInWithCredentialError ->
                                        if(signInWithCredentialError::class == FirebaseAuthInvalidUserException::class) {
                                            hostActivity.onFragmentFinish(AuthActivity.Result.ERROR_LOGIN_ACCOUNT_DISABLED, signInWithCredentialError)
                                            return@addOnFailureListener
                                        }
                                        hostActivity.onFragmentFinish(AuthActivity.Result.ERROR_GOOGLE_LOGIN_DEVELOPER_ERROR, signInWithCredentialError)
                                    }
                                    .addOnCompleteListener {
                                        binding.progressBarLogin.isIndeterminate = false
                                    }

                            } catch (e: ApiException) {
                                binding.progressBarLogin.isIndeterminate = false
                                hostActivity.onFragmentFinish(AuthActivity.Result.ERROR_GOOGLE_LOGIN_DEVELOPER_ERROR, e)
                            }
                        } else {
                            binding.progressBarLogin.isIndeterminate = false
                            hostActivity.onFragmentFinish(AuthActivity.Result.ERROR_GOOGLE_LOGIN_DEVELOPER_ERROR, Exception("Auth-Task not successful"))
                        }
                    }
                    .addOnFailureListener { signedInAccountFromIntentError ->
                        binding.progressBarLogin.isIndeterminate = false
                        hostActivity.onFragmentFinish(AuthActivity.Result.ERROR_GOOGLE_LOGIN_DEVELOPER_ERROR, signedInAccountFromIntentError)
                    }
            }
        }
    }
}