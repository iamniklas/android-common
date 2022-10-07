package de.niklasenglmeier.androidcommon.auth.fragments

import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import de.niklasenglmeier.androidcommon.auth.AuthActivity
import de.niklasenglmeier.androidcommon.activitydata.AuthenticationData
import de.niklasenglmeier.androidcommon.databinding.FragmentRegisterBinding
import de.niklasenglmeier.androidcommon.extensions.LongExtensions.flagIsSet
import de.niklasenglmeier.androidcommon.extensions.TextInputLayoutExtensions.validateEmailInput
import de.niklasenglmeier.androidcommon.extensions.TextInputLayoutExtensions.validatePasswordInput
import de.niklasenglmeier.androidcommon.extensions.TextInputLayoutExtensions.validatePasswordRepeat
import de.niklasenglmeier.androidcommon.firebase.auth.FirebaseAuthHandler
import de.niklasenglmeier.androidcommon.firebase.firestore.FirestoreStandardFetches
import de.niklasenglmeier.androidcommon.firebase.firestore.FirestoreStandardPushes
import de.niklasenglmeier.androidcommon.firebase.remoteconfig.RemoteConfigFetches
import de.niklasenglmeier.androidcommon.models.ResultCode
import de.niklasenglmeier.androidcommon.models.standard.LoginMethod

class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding

    private lateinit var authenticationData: AuthenticationData

    private lateinit var hostActivity: AuthActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(layoutInflater)

        hostActivity = requireActivity() as AuthActivity

        authenticationData = hostActivity.authData

        if(authenticationData.authIcon != null) {
            binding.imageViewRegister.setImageDrawable(ContextCompat.getDrawable(requireContext(), authenticationData.authIcon!!))
        } else {
            binding.imageViewRegister.visibility = View.GONE
        }

        if(authenticationData.flags.flagIsSet(AuthenticationData.Flags.FIREBASE_USE_REMOTE_CONFIG)) {
            RemoteConfigFetches.getRemoteConfig(
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
                    hostActivity.onFragmentFinish(ResultCode.ERROR, remoteConfigFetchError)
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

            binding.progressBarLogin.isIndeterminate = true

            FirebaseAuthHandler
                .Email
                .performEmailRegistration(
                    email,
                    password,
                    {
                        Toast.makeText(requireContext(), "Please verify your email address by following the instructions we sent you to $email", Toast.LENGTH_LONG).show()
                        Firebase.auth.currentUser!!.sendEmailVerification()
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Verification Email sent", Toast.LENGTH_LONG).show()
                            }
                            .addOnFailureListener { sendEmailVerificationError ->
                                Toast.makeText(requireContext(), "Verification Email Error ${sendEmailVerificationError.message.toString()}", Toast.LENGTH_LONG).show()
                            }
                            .addOnCompleteListener {
                                if(authenticationData.flags.flagIsSet(AuthenticationData.Flags.FIREBASE_USE_FIRESTORE)) {
                                    FirestoreStandardFetches
                                        .Users
                                        .getUserInfo(
                                            true,
                                            {
                                                binding.progressBarLogin.isIndeterminate = false
                                                hostActivity.onFragmentFinish(ResultCode.SUCCESS)
                                            },
                                            {
                                                //User Data does not exist
                                                FirestoreStandardPushes.Users.createNewUserEntry(
                                                    LoginMethod.Email,
                                                    null,
                                                    null,
                                                    {
                                                        binding.progressBarLogin.isIndeterminate = false
                                                        hostActivity.toggleView()
                                                    },
                                                    { userDataCreationError ->
                                                        binding.progressBarLogin.isIndeterminate = false
                                                        hostActivity.onFragmentFinish(ResultCode.ERROR, userDataCreationError)
                                                    })
                                            },
                                            {
                                                Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                                            }
                                        )
                                } else {
                                    binding.progressBarLogin.isIndeterminate = false
                                    hostActivity.onFragmentFinish(ResultCode.SUCCESS)
                                }
                            }
                    },
                    { emailRegistrationError ->
                        Toast.makeText(requireContext(), emailRegistrationError.message, Toast.LENGTH_LONG).show()
                        hostActivity.onFragmentFinish(ResultCode.ERROR, emailRegistrationError)
                    },
                    { }
                )
        }

        binding.textViewRegisterLogin.setOnClickListener {
            hostActivity.toggleView()
        }

        return binding.root
    }
}