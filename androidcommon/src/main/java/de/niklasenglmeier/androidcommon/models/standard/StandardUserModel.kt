package de.niklasenglmeier.androidcommon.models.standard

class StandardUserModel(var userId: String,
                        var loginMethod: LoginMethod,
                        var displayName: String?,
                        var email: String?,
                        var phoneNumber: String?,
                        var firstName: String?,
                        var lastName: String?,
                        var userLevel: UserLevel) {

    companion object
}