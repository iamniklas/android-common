package de.niklasenglmeier.androidcommon.models.standard

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import de.niklasenglmeier.androidcommon.firebase.firestore.FirestoreObject

class StandardGroupModel(
    var dateOfCreation: Timestamp,
    var groupName: String,
    @Exclude
    var users: ArrayList<StandardGroupModel>,
    var userReferences: ArrayList<DocumentReference>): FirestoreObject(Firebase.firestore.collection("Groups")) {

    constructor() : this(Timestamp.now(), "", arrayListOf(), arrayListOf())

    companion object
}