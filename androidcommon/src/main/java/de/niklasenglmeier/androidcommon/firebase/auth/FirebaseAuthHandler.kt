package de.niklasenglmeier.androidcommon.firebase.auth

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

object FirebaseAuthHandler {
    object Google {
        fun performGoogleLogin() {

        }
    }

    object Email {
        fun performEmailRegistration(email: String,
                                     password: String,
                                     onSuccessListener: OnSuccessListener<AuthResult>,
                                     onFailureListener: OnFailureListener,
                                     onCompleteListener: OnCompleteListener<AuthResult>) {
            Firebase
                .auth
                .createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    onSuccessListener.onSuccess(it)
                }
                .addOnFailureListener {
                    onFailureListener.onFailure(it)
                }
                .addOnCompleteListener {
                    onCompleteListener.onComplete(it)
                }
        }

        fun performEmailLogin(email: String, password: String, onSuccessListener: OnSuccessListener<AuthResult>, onFailureListener: OnFailureListener) {
            Firebase
                .auth
                .signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    onSuccessListener.onSuccess(it)
                }
                .addOnFailureListener {
                    onFailureListener.onFailure(it)
                }
        }

        fun sendEmailVerification() {
            Firebase
                .auth
                .currentUser!!
                .sendEmailVerification()
        }

        fun sendPasswordResetEmail(email: String, onSuccessListener: OnSuccessListener<Void>, onFailureListener: OnFailureListener) {
            Firebase
                .auth
                .sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    onSuccessListener.onSuccess(it)
                }
                .addOnFailureListener {
                    onFailureListener.onFailure(it)
                }
        }
    }
}