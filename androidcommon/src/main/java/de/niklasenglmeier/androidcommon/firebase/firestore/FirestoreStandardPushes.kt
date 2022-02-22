package de.niklasenglmeier.androidcommon.firebase.firestore

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import de.niklasenglmeier.androidcommon.models.standard.StandardUserModel

object FirestoreStandardPushes {
    object Users {
        fun createNewUserEntry(onSuccessListener: OnSuccessListener<StandardUserModel>,
                               onFailureListener: OnFailureListener
        ) {
            val displayName = if (Firebase.auth.currentUser!!.displayName == null) Firebase.auth.currentUser!!.email!! else Firebase.auth.currentUser!!.displayName!!

            val user =  StandardUserModel(
                Firebase.auth.currentUser!!.uid,
                Timestamp.now(),
                Timestamp.now(),
                displayName,
                false
            )

            Firebase.firestore
                .collection("Users")
                .document(Firebase.auth.currentUser!!.uid)
                .set(user.toHashMap())
                .addOnSuccessListener { onSuccessListener.onSuccess(user) }
                .addOnFailureListener { onFailureListener.onFailure(it) }
        }
    }

    fun StandardUserModel.toHashMap() : HashMap<String, Any> {
        return hashMapOf(
            "date_of_creation" to dateOfCreation,
            "display_name" to displayName,
            "last_login" to lastLogin,
        )
    }
}