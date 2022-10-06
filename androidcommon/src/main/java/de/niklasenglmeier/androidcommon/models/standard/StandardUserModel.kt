package de.niklasenglmeier.androidcommon.models.standard

import com.google.firebase.Timestamp

class StandardUserModel(var userId: String,
                        var loginMethod: LoginMethod,
                        var dateOfCreation: Timestamp,
                        var lastLogin: Timestamp,
                        var displayName: String?,
                        var firstName: String?,
                        var lastName: String?,
                        var userLevel: UserLevel) {

    //Display Name User Profile
    constructor(_userId: String,
                _loginMethod: LoginMethod,
                _dateOfCreation: Timestamp,
                _lastLogin: Timestamp,
                _displayName: String,
                _userLevel: UserLevel) :
            this(
                _userId,
                _loginMethod,
                _dateOfCreation,
                _lastLogin,
                _displayName,
                null,
                null,
                _userLevel
            )
    
    //Firt- & Last Name User Profile
    constructor(_userId: String,
                _loginMethod: LoginMethod,
                _dateOfCreation: Timestamp,
                _lastLogin: Timestamp,
                _firstName: String,
                _lastName: String,
                _userLevel: UserLevel)  :
            this(
                _userId,
                _loginMethod,
                _dateOfCreation,
                _lastLogin,
                null,
                _firstName,
                _lastName,
                _userLevel
            )

    companion object
}