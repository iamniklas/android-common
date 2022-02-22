package de.niklasenglmeier.androidcommon

import de.niklasenglmeier.androidcommon.activitydata.AuthenticationData
import de.niklasenglmeier.androidcommon.extensions.IntExtensions.flagIsSet
import de.niklasenglmeier.androidcommon.firebase.FirebaseInteractions
import org.junit.Assert
import org.junit.Test

class FlagsTest {
    @Test
    fun testBinaryInteger() {
        Assert.assertEquals(15, 0b0000_1111)
    }

    @Test
    fun testFirebaseInteractions() {
        val aad = AuthenticationData(
            appName = "AppName",
            showAppNameInSupportActionBar = true,
            firebaseInteractionMask = FirebaseInteractions.Authorization or FirebaseInteractions.FirestoreUser,
            authTypes = 0,
            googleAuthId = null)

        Assert.assertTrue(aad.firebaseInteractionMask.flagIsSet(FirebaseInteractions.Authorization))
        Assert.assertTrue(aad.firebaseInteractionMask.flagIsSet(FirebaseInteractions.FirestoreUser))
        Assert.assertFalse(aad.firebaseInteractionMask.flagIsSet(FirebaseInteractions.RemoteConfig))
    }
}