package de.niklasenglmeier.androidcommon.models.standard

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import de.niklasenglmeier.androidcommon.firebase.firestore.FirestoreObject

class StandardUserModel(var loginMethod: LoginMethod,
                        var displayName: String?,
                        var email: String?,
                        var phoneNumber: String?,
                        var firstName: String?,
                        var lastName: String?,
                        var userLevel: UserLevel) : FirestoreObject(Firebase.firestore.collection("Users")) {

    constructor() : this(LoginMethod.Email, null, null, null, null, null, UserLevel.Default)
}