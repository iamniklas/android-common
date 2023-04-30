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
                loginMethod,
                displayName,
                email,
                phone,
                firstName,
                lastName,
                UserLevel.Default
            )

            user.push(
                { onSuccessListener.onSuccess(it as StandardUserModel) },
                { onFailureListener.onFailure(it) }
            )
        }
    }
}