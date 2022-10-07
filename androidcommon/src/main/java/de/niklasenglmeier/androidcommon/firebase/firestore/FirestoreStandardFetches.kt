package de.niklasenglmeier.androidcommon.firebase.firestore

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import de.niklasenglmeier.androidcommon.models.standard.LoginMethod
import de.niklasenglmeier.androidcommon.models.standard.StandardUserModel
import de.niklasenglmeier.androidcommon.models.standard.UserLevel

object FirestoreStandardFetches {

    object Users {
        private var userInfoCache: StandardUserModel? = null
        fun getUserInfo(flushCache: Boolean, onSuccessListener: OnSuccessListener<StandardUserModel>, userDataDoesNotExists: OnFailureListener, onFailureListener: OnFailureListener) {
            if(userInfoCache != null && !flushCache) {
                onSuccessListener.onSuccess(userInfoCache)
                return
            }

            Firebase.firestore
                .collection("Users")
                .document(Firebase.auth.uid!!)
                .get()
                .addOnSuccessListener {
                    if(!it.exists()) {
                        userDataDoesNotExists.onFailure(Exception())
                        return@addOnSuccessListener
                    }

                    userInfoCache = StandardUserModel.fromDocument(it)
                    onSuccessListener.onSuccess(userInfoCache)
                }
                .addOnFailureListener {
                    onFailureListener.onFailure(it)
                }
        }
    }

    fun StandardUserModel.Companion.fromDocument(docSnap: DocumentSnapshot) : StandardUserModel {
        return StandardUserModel(
            docSnap.id,
            LoginMethod.valueOf(docSnap.getString("login_method")!!),
            docSnap.getString("display_name"),
            docSnap.getString("email"),
            docSnap.getString("phone_number"),
            docSnap.getString("first_name"),
            docSnap.getString("last_name"),
            UserLevel.valueOf(docSnap.getString("user_level")!!)
        )
    }
}