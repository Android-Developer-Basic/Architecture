package ru.otus.architecture.permissions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

fun Context.goToSettings() {
    startActivity(Intent().apply {
        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        data = Uri.fromParts("package", packageName, null)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    })
}