package de.niklasenglmeier.androidcommon.firebase.firestore

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import de.niklasenglmeier.androidcommon.models.standard.LoginMethod
import de.niklasenglmeier.androidcommon.models.standard.StandardUserModel
import de.niklasenglmeier.androidcommon.models.standard.UserLevel

object FirestoreStandardPushes {
    object Users {
        fun createNewUserEntry(loginMethod: LoginMethod,
                               firstName: String?,
                               lastName: String?,
                               onSuccessListener: OnSuccessListener<StandardUserModel>,
                               onFailureListener: OnFailureListener
        ) {
            val displayName = Firebase.auth.currentUser!!.displayName
            val email = Firebase.auth.currentUser!!.email
            val phone = Firebase.auth.currentUser!!.phoneNumber

            val user =  StandardUserModel(
                Firebase.auth.currentUser!!.uid,
                loginMethod,
                displayName,
                email,
                phone,
                firstName,
                lastName,
                UserLevel.Default
            )

            Firebase.firestore
                .collection("Users")
                .document(Firebase.auth.currentUser!!.uid)
                .set(user.toHashMap())
                .addOnSuccessListener { onSuccessListener.onSuccess(user) }
                .addOnFailureListener { onFailureListener.onFailure(it) }
        }
    }

    fun StandardUserModel.toHashMap() : HashMap<String, String?> {
        return hashMapOf(
            "login_method" to loginMethod.toString(),
            "display_name" to displayName,
            "email" to email,
            "phone" to phoneNumber,
            "first_name" to firstName,
            "last_name" to lastName,
            "user_level" to userLevel.toString()
        )
    }
}