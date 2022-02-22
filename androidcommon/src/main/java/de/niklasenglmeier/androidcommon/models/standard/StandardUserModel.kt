package de.niklasenglmeier.androidcommon.models.standard

import com.google.firebase.Timestamp

class StandardUserModel(
    var userId: String,
    var dateOfCreation: Timestamp,
    var lastLogin: Timestamp,
    var displayName: String,
    var isAdmin: Boolean) {

    companion object
}