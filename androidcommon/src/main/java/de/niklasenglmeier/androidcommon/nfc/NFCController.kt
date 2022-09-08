package de.niklasenglmeier.androidcommon.nfc

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import java.io.UnsupportedEncodingException
import java.util.regex.Pattern
import kotlin.experimental.and

class NFCController(var activity: Activity, var pendingIntent: PendingIntent, nfcCallback: NFCCallback) {

    var nfcAdapter: NfcAdapter? = null

    init {
        nfcAdapter = NfcAdapter.getDefaultAdapter(activity)
        if (nfcAdapter == null) {
            nfcCallback.onNFCNotSupported()
        }
    }

    fun onPause() {
        if (nfcAdapter != null) {
            nfcAdapter!!.disableForegroundDispatch(activity)
        }
    }

    fun onResume() {
        if (nfcAdapter == null) {
            return
        }
        nfcAdapter!!.enableForegroundDispatch(activity, pendingIntent, null, null)
    }

    companion object {
        fun buildTagViews(msgs: MutableList<NdefMessage>) : String {
            if (msgs.size == 0) return ""

            var text = ""
            val payload = msgs[0].records[0].payload
            //val textEncoding = if ((payload[0] and 128.toByte()) == (0.toByte())) "UTF-8" else "UTF-16"
            val languageCodeLength = payload[0] and 63

            try {
                return String(payload, languageCodeLength + 1, payload.size - languageCodeLength - 1)
            }
            catch (e: UnsupportedEncodingException) {
                return ""
            }
        }
    }
}