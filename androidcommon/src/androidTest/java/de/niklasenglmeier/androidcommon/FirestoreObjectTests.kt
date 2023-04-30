package de.niklasenglmeier.androidcommon

import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.FirebaseApp
import de.niklasenglmeier.androidcommon.models.standard.LoginMethod
import de.niklasenglmeier.androidcommon.models.standard.StandardUserModel
import de.niklasenglmeier.androidcommon.models.standard.UserLevel
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class FirestoreObjectTests {
    @Before
    fun initialize() {
        FirebaseApp.initializeApp(InstrumentationRegistry.getInstrumentation().context)
    }

    @Test
    fun testObjectToMap() {
        val userModel = StandardUserModel(LoginMethod.Email, "Test User", "test@androidcommon.de", "0123456789", "Test", "User", UserLevel.Default)

        val objectMap = userModel.objectToMap()

        Assert.assertEquals(7, objectMap.size)

        println(objectMap.map { it.key })
        println(objectMap.map { it.value })
    }
}