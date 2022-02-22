package de.niklasenglmeier.androidcommon.activitydata

import android.os.Parcel
import android.os.Parcelable
import de.niklasenglmeier.androidcommon.firebase.auth.AuthTypes

class AuthenticationData(val appName: String,
                         val showAppNameInSupportActionBar: Boolean,
                         val firebaseInteractionMask: Int,
                         val authTypes: Int,
                         val googleAuthId : String?) : Parcelable {

    object IntentKey {
        const val PARCELABLE_EXTRA = "auth_activities_data"
    }

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readInt() == 1,
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(appName)
        parcel.writeInt(if (showAppNameInSupportActionBar) 1 else 0)
        parcel.writeInt(firebaseInteractionMask)
        parcel.writeInt(authTypes)
        parcel.writeString(googleAuthId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AuthenticationData> {
        override fun createFromParcel(parcel: Parcel): AuthenticationData {
            return AuthenticationData(parcel)
        }

        override fun newArray(size: Int): Array<AuthenticationData?> {
            return arrayOfNulls(size)
        }
    }
}