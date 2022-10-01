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
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import de.niklasenglmeier.androidcommon.activitydata.AuthenticationData
import de.niklasenglmeier.androidcommon.alertdialogs.Dialogs
import de.niklasenglmeier.androidcommon.databinding.FragmentLoginBinding
import de.niklasenglmeier.androidcommon.extensions.LongExtensions.flagIsSet
import de.niklasenglmeier.androidcommon.extensions.TextInputLayoutExtensions.validateEmailInput
import de.niklasenglmeier.androidcommon.extensions.TextInputLayoutExtensions.validatePasswordInput
import de.niklasenglmeier.androidcommon.firebase.auth.FirebaseAuthHandler
import de.niklasenglmeier.androidcommon.firebase.firestore.FirestoreStandardFetches
import de.niklasenglmeier.androidcommon.firebase.firestore.FirestoreStandardPushes
import de.niklasenglmeier.androidcommon.firebase.remoteconfig.RemoteConfigFetches
import de.niklasenglmeier.androidcommon.models.ResultCode

class LoginFragment : Fragment() {

    private val RC_SIGN_IN = 1
    private val RC_REGISTER = 2

    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var binding: FragmentLoginBinding

    private lateinit var authenticationData: AuthenticationData

    private lateinit var hostActivity: de.niklasenglmeier.androidcommon.auth.AuthActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(layoutInflater)

        hostActivity = requireActivity() as de.niklasenglmeier.androidcommon.auth.AuthActivity

        authenticationData = hostActivity.authData

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
                        hostActivity.onFragmentFinish(ResultCode.ERROR, it)
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
                                                Toast.makeText(
                                                    requireContext(),
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
                                            hostActivity.onFragmentFinish(ResultCode.SUCCESS)
                                        },
                                        {
                                            //User Data does not exist
                                            FirestoreStandardPushes.Users.createNewUserEntry(
                                                {
                                                    hostActivity.onFragmentFinish(ResultCode.SUCCESS)
                                                },
                                                { userError ->
                                                    hostActivity.onFragmentFinish(ResultCode.ERROR, userError)
                                                })
                                        },
                                        {
                                            hostActivity.onFragmentFinish(ResultCode.ERROR, it)
                                        }
                                    )
                            }
                            else {
                                hostActivity.onFragmentFinish(ResultCode.SUCCESS)
                            }
                        }
                    }
                ) {
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
                                                        hostActivity.onFragmentFinish(ResultCode.SUCCESS)
                                                    },
                                                    {
                                                        //User Data does not exist
                                                        FirestoreStandardPushes
                                                            .Users
                                                            .createNewUserEntry(
                                                                {
                                                                    hostActivity.onFragmentFinish(ResultCode.SUCCESS)
                                                                },
                                                                {
                                                                    hostActivity.onFragmentFinish(ResultCode.ERROR, it)
                                                                }
                                                            )
                                                    },
                                                    {
                                                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                                                    })
                                        } else {
                                            hostActivity.onFragmentFinish(ResultCode.SUCCESS)
                                        }
                                    }
                                    .addOnFailureListener { signInWithCredentialError ->
                                        hostActivity.onFragmentFinish(ResultCode.ERROR, signInWithCredentialError)
                                    }
                                    .addOnCompleteListener {
                                        binding.progressBarLogin.isIndeterminate = false
                                    }

                            } catch (e: ApiException) {
                                binding.progressBarLogin.isIndeterminate = false
                                hostActivity.onFragmentFinish(ResultCode.ERROR, e)
                            }
                        } else {
                            binding.progressBarLogin.isIndeterminate = false
                            hostActivity.onFragmentFinish(ResultCode.ERROR, Exception("Auth-Task not successful"))
                        }
                    }
                    .addOnFailureListener { signedInAccountFromIntentError ->
                        binding.progressBarLogin.isIndeterminate = false
                        hostActivity.onFragmentFinish(ResultCode.ERROR, signedInAccountFromIntentError)
                    }
            }

            RC_REGISTER -> {

            }
        }
    }

    companion object {
        const val RESULT_EXTRA_ERROR_MESSAGE = "error_message"
    }
}