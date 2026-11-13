package com.lihan.qrnest.core.presentation.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.Settings
import androidx.core.net.toUri

class AndroidExternalAppLauncher(
    private val context: Context
) : ExternalAppLauncher {

    override fun openUrl(url: String) {
        val normalized = if (url.startsWith("http://", ignoreCase = true) ||
            url.startsWith("https://", ignoreCase = true)
        ) {
            url
        } else {
            "https://$url"
        }
        val intent = Intent(Intent.ACTION_VIEW, normalized.toUri()).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }

    override fun dialPhone(phone: String) {
        val cleanNumber = phone.removePrefix("tel:").trim()
        val intent = Intent(Intent.ACTION_DIAL, "tel:$cleanNumber".toUri()).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }

    override fun openGeo(latLng: String) {
        val parts = latLng.removePrefix("geo:").split(",")
        val lat = parts.getOrNull(0)?.trim()?.toDoubleOrNull() ?: 0.0
        val lng = parts.getOrNull(1)?.trim()?.toDoubleOrNull() ?: 0.0
        val geoIntent = Intent(Intent.ACTION_VIEW, "geo:$lat,$lng?q=$lat,$lng".toUri()).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val started = runCatching { context.startActivity(geoIntent) }.isSuccess
        if (!started) {
            openUrl("https://www.google.com/maps/search/?api=1&query=$lat,$lng")
        }
    }

    override fun insertContact(name: String, email: String, phone: String) {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            type = ContactsContract.Contacts.CONTENT_TYPE
            putExtra(ContactsContract.Intents.Insert.NAME, name)
            if (phone.isNotBlank()) {
                putExtra(ContactsContract.Intents.Insert.PHONE, phone)
            }
            if (email.isNotBlank()) {
                putExtra(ContactsContract.Intents.Insert.EMAIL, email)
            }
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }

    override fun shareText(text: String) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val chooser = Intent.createChooser(sendIntent, null).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(chooser) }
    }

    override fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }

    override fun openWifiSettings() {
        val action = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Settings.Panel.ACTION_WIFI
        } else {
            Settings.ACTION_WIFI_SETTINGS
        }
        val intent = Intent(action).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }
}
