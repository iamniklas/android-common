package de.niklasenglmeier.androidcommon.alertdialogs

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import de.niklasenglmeier.androidcommon.R


object Dialogs {
    fun makeLoginErrorDialog(activity: Activity, onPositiveButtonClicked: () -> Unit) : AlertDialog {
        return AlertDialog.Builder(activity)
            .setTitle("Login Error")
            .setCancelable(false)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(android.R.string.ok) { _, _ -> onPositiveButtonClicked() }
            .apply {
                setMessage("Email login failed! You either entered invalid credentials or you don't have an account yet.")
            }
            .create()
    }

    fun makeEmailNotVerifiedErrorDialog(activity: Activity, onPositiveButtonClicked: () -> Unit, onResendVerificationButtonClicked: () -> Unit) : AlertDialog {
        return AlertDialog.Builder(activity)
            .setTitle("Email not verified")
            .setCancelable(false)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(android.R.string.ok) { _, _ -> onPositiveButtonClicked() }
            .setNeutralButton("Send verification mail") { _, _ -> onResendVerificationButtonClicked() }
            .apply {
                setMessage("The entered email address is not verified yet. Please check your mails or request a new verification mail")
            }
            .create()
    }

    fun makeRemoteConfigFetchErrorDialog(activity: Activity, onPositiveButtonClicked: () -> Unit) : AlertDialog {
        return AlertDialog.Builder(activity)
            .setTitle(activity.applicationContext.getString(R.string.alert_dialog_remote_config_error_title))
            .setCancelable(false)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton("Retry") { _, _ -> onPositiveButtonClicked() }
            .apply {
                setMessage(context.getString(R.string.alert_dialog_remote_config_error_fetch))
            }
            .create()
    }

    enum class NetworkErrorType { INVALID_CREDENTIALS, INVALID_BROKER_ADDRESS, NETWORK_TIMEOUT }
    fun makeNetworkErrorDialog(activity: Activity, type: NetworkErrorType, onPositiveButtonClicked: () -> Unit) : AlertDialog {
        return AlertDialog.Builder(activity)
            .setTitle(activity.applicationContext.getString(R.string.alert_dialog_network_error_title))
            .setCancelable(false)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton("Settings") { _, _ -> onPositiveButtonClicked() }
            .apply {
                val msg = when (type) {
                    NetworkErrorType.INVALID_CREDENTIALS -> context.getString(R.string.alert_dialog_network_error_invalid_credentials)
                    NetworkErrorType.INVALID_BROKER_ADDRESS -> context.getString(R.string.alert_dialog_network_error_invalid_broker_address)
                    NetworkErrorType.NETWORK_TIMEOUT -> context.getString(R.string.alert_dialog_network_error_network_timeout)
                }

                setMessage(msg)
            }
            .create()
    }

    enum class ActionTagErrorType { CHIP_DATA_ERROR, CLOUD_ERROR_NO_DATA, CLOUD_ERROR_INVALID_DATA }
    fun makeActionTagError(activity: Activity, type: ActionTagErrorType, onPositiveButtonClicked: () -> Unit) : AlertDialog {
        return AlertDialog.Builder(activity)
            .setTitle(activity.applicationContext.getString(R.string.alert_dialog_action_tag_error_title))
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok) { _, _ -> onPositiveButtonClicked() }
            .apply {
                val msg = when (type) {
                    ActionTagErrorType.CHIP_DATA_ERROR -> context.getString(R.string.alert_dialog_action_tag_error_chip_data)
                    ActionTagErrorType.CLOUD_ERROR_NO_DATA -> context.getString(R.string.alert_dialog_action_tag_error_cloud_no_data)
                    ActionTagErrorType.CLOUD_ERROR_INVALID_DATA -> context.getString(R.string.alert_dialog_action_tag_error_cloud_invalid_data)
                }
                setMessage(msg)
            }
            .create()
    }

    enum class DeviceLinkErrorType { NETWORK_ERROR, DATABASE_ERROR, INVALID_NAME }
    fun makeDeviceLinkError(activity: Activity, type: DeviceLinkErrorType, onPositiveButtonClicked: () -> Unit) : AlertDialog {
        return AlertDialog.Builder(activity)
            .setTitle(activity.applicationContext.getString(R.string.alert_dialog_device_link_error_title))
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok) { _, _ -> onPositiveButtonClicked() }
            .apply {
                val msg = when (type) {
                    DeviceLinkErrorType.NETWORK_ERROR -> context.getString(R.string.alert_dialog_device_link_error_network)
                    DeviceLinkErrorType.DATABASE_ERROR -> context.getString(R.string.alert_dialog_device_link_error_database)
                    DeviceLinkErrorType.INVALID_NAME -> context.getString(R.string.alert_dialog_device_link_error_invalid_name)
                }
                setMessage(msg)
            }
            .create()
    }

    enum class ConfigurationErrorType { STORE_ERROR, READ_ERROR, INVALID_DATA }
    fun makeConfigurationError(activity: Activity, type: ConfigurationErrorType, onPositiveButtonClicked: () -> Unit) : AlertDialog {
        return AlertDialog.Builder(activity)
            .setTitle(activity.applicationContext.getString(R.string.alert_dialog_configuration_error_title))
            .setCancelable(false)
            .setPositiveButton("Settings") { _, _ -> onPositiveButtonClicked() }
            .apply {
                val msg = when (type) {
                    ConfigurationErrorType.STORE_ERROR -> context.getString(R.string.alert_dialog_device_link_error_network)
                    ConfigurationErrorType.READ_ERROR -> context.getString(R.string.alert_dialog_device_link_error_network)
                    ConfigurationErrorType.INVALID_DATA -> context.getString(R.string.alert_dialog_device_link_error_network)
                }
                setMessage(msg)
            }
            .create()
    }

    enum class InvalidScriptErrorType { CLOUD_SAVE_ERROR, SYNTAX_ERROR, OTHER_CODE_ERROR }
    fun makeInvalidScriptFileError(activity: Activity, type: InvalidScriptErrorType, stackTrace: String?, onPositiveButtonClicked: () -> Unit) : AlertDialog {
        return AlertDialog.Builder(activity)
            .setTitle(activity.applicationContext.getString(R.string.alert_dialog_invalid_script_error_title))
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok) { _, _ -> onPositiveButtonClicked() }
            .apply {
                val msg = when (type) {
                    InvalidScriptErrorType.CLOUD_SAVE_ERROR -> context.getString(R.string.alert_dialog_invalid_script_error_cloud_save)
                    InvalidScriptErrorType.SYNTAX_ERROR -> context.getString(R.string.alert_dialog_invalid_script_error_syntax, stackTrace)
                    InvalidScriptErrorType.OTHER_CODE_ERROR -> context.getString(R.string.alert_dialog_invalid_script_error_other)
                }

                setMessage(msg)
            }
            .create()
    }

    object Custom {
        fun makeYesNoDialog(activity: Activity,
                            title: String,
                            body: String,
                            imageResource: Int?,
                            onYesButtonClick : DialogInterface.OnClickListener,
                            onNoButtonClick : DialogInterface.OnClickListener) : AlertDialog {
            return AlertDialog.Builder(activity).apply {
                setTitle(title)
                setMessage(body)
                setPositiveButton("Yes", onYesButtonClick)
                setNegativeButton("No", onNoButtonClick)

                if(imageResource != null) {
                    setIcon(imageResource)
                }
            }.create()
        }

        fun makeOkDialog(activity: Activity,
                         title: String,
                         body: String,
                         imageResource: Int?,
                         onOkButtonClick : DialogInterface.OnClickListener) : AlertDialog {
            return AlertDialog.Builder(activity).apply {
                setTitle(title)
                setMessage(body)
                setPositiveButton(android.R.string.ok, onOkButtonClick)

                if(imageResource != null) {
                    setIcon(imageResource)
                }
            }.create()
        }

        fun makeRetryDialog(activity: Activity,
                            title: String,
                            body: String,
                            imageResource: Int?,
                            onRetryButtonClick : DialogInterface.OnClickListener) : AlertDialog {
            return AlertDialog.Builder(activity).apply {
                setTitle(title)
                setMessage(body)
                setPositiveButton("Retry", onRetryButtonClick)

                if(imageResource != null) {
                    setIcon(imageResource)
                }
            }.create()
        }
    }
}