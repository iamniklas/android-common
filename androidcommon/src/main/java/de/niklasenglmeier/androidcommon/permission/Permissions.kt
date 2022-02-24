package de.niklasenglmeier.androidcommon.permission

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object Permissions {
    /**
     * @param permission The name of the requested permission (Manifest.permission.PERMISSION_NAME)
     * @return True if permission is given, otherwise false
     */
    fun permissionIsGiven(context: Context, permission: String) : Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * @param permission The name of the requested permission (Manifest.permission.PERMISSION_NAME)
     * @param requestCode The request code for the permission callback interface (onRequestPermissionsResult)
     */
    fun requestPermission(activity: Activity, permission: String, requestCode: Int) : Boolean {
        if(!permissionIsGiven(activity.applicationContext, permission)) {
            ActivityCompat.requestPermissions(activity, arrayOf(permission), requestCode)
            return true
        } else {
            return false
        }
    }
}