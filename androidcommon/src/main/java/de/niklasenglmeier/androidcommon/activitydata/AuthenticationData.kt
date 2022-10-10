package de.niklasenglmeier.androidcommon.activitydata

import android.os.Parcel
import android.os.Parcelable

class AuthenticationData(val appName: String,
                         val showAppNameInSupportActionBar: Boolean,
                         val authIcon: Int?,
                         val flags: Long,
                         val googleAuthId : String?) : Parcelable {

    object Flags {
        const val EMAIL_LOGIN : Long =                          0b0000_0000_0000_0001
        const val GOOGLE_LOGIN : Long =                         0b0000_0000_0000_0010
        const val ANONYMOUS_LOGIN: Long =                       0b0000_0000_0000_0100
        const val LOGIN_OPTIONAL : Long =                       0b0000_0000_0000_1000

        const val SHOW_APP_ICON : Long =                        0b0000_0000_0001_0000
        const val SHOW_APP_NAME_IN_ACTION_BAR : Long =          0b0000_0000_0010_0000

        const val FIREBASE_USE_FIRESTORE : Long =               0b0000_0001_0000_0000
        const val FIREBASE_USE_REMOTE_CONFIG : Long =           0b0000_0010_0000_0000

        const val ALL_NO_OPTIONAL_LOGIN : Long =                0b1111_1111_1111_0111
        const val ALL_NO_OPTIONAL_NO_ANONYMOUS_LOGIN: Long =    0b1111_1111_1111_0011
        const val ALL_NO_ANONYMOUS_LOGIN: Long =                0b1111_1111_1111_1011
        const val ALL : Long =                                  0b1111_1111_1111_1111
    }

    object IntentKey {
        const val PARCELABLE_EXTRA = "auth_activities_data"
    }

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readInt() == 1,
        parcel.readInt(),
        parcel.readLong(),
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(appName)
        parcel.writeInt(if (showAppNameInSupportActionBar) 1 else 0)
        parcel.writeInt(authIcon ?: -1)
        parcel.writeLong(this.flags)
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