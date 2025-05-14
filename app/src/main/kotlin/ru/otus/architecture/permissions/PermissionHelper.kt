package ru.otus.architecture.permissions

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * Helper to request permission on start of the activity
 */
class PermissionHelper(
    private val context: Context,
    private val registry: ActivityResultRegistry,
    private val permissions: Set<String>,
    private val onGranted: () -> Unit,
    private val onDenied: (Set<String>) -> Unit
) : DefaultLifecycleObserver {

    private val key = "PermissionHelper:$permissions"

    private var launcher: ActivityResultLauncher<Array<String>>? = null

    private fun checkAlreadyGranted() = permissions.all {
        PackageManager.PERMISSION_GRANTED == context.checkSelfPermission(it)
    }

    override fun onCreate(owner: LifecycleOwner) {
        if (checkAlreadyGranted()) {
            onGranted()
            return
        }

        launcher = registry.register(key, ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val granted = result.filterValues { it }.keys
            if (granted.containsAll(permissions)) {
                onGranted()
            } else {
                onDenied(permissions - granted)
            }
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        launch()
    }

    /**
     * Retry permission request
     */
    fun launch() {
        if (checkAlreadyGranted()) {
            onGranted()
            return
        }
        requireNotNull(launcher) { "Permission launcher is not registered" }.launch(permissions.toTypedArray())
    }

    override fun onDestroy(owner: LifecycleOwner) {
        launcher?.unregister()
    }
}