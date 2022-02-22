package de.niklasenglmeier.androidcommon.models.standard

import com.google.firebase.Timestamp

class StandardGroupModel(
    var groupId: String,
    var dateOfCreation: Timestamp,
    var groupName: String,
    var users: ArrayList<StandardGroupModel>) {

    companion object
}