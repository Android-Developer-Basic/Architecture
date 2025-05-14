package ru.otus.architecture.permissions

import android.Manifest
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.app.ActivityOptionsCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class PermissionHelperTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val permissions = setOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private fun createRegistry(result: Map<String, Boolean>): ActivityResultRegistry {
        return object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(
                requestCode: Int,
                contract: ActivityResultContract<I, O>,
                input: I,
                options: ActivityOptionsCompat?
            ) {
                dispatchResult(requestCode, result)
            }
        }
    }

    @Test
    fun callsGrantedOnSuccess() {
        val result = mapOf(
            Manifest.permission.ACCESS_COARSE_LOCATION to true,
            Manifest.permission.ACCESS_FINE_LOCATION to true
        )

        var granted = false
        var denied = false
        val permissionHelper = PermissionHelper(
            ApplicationProvider.getApplicationContext(),
            createRegistry(result),
            permissions,
            { granted = true },
            { denied = true }
        )
        permissionHelper.onCreate(mockk())
        permissionHelper.launch()

        assert(granted) { "Not Granted" }
        assert(denied.not()) { "Denied" }
    }

    @Test
    fun callsDeniedOnFailure() {
        val result = mapOf(
            Manifest.permission.ACCESS_COARSE_LOCATION to true,
            Manifest.permission.ACCESS_FINE_LOCATION to false
        )

        var granted = false
        var denied: Set<String>? = null
        val permissionHelper = PermissionHelper(
            ApplicationProvider.getApplicationContext(),
            createRegistry(result),
            permissions,
            { granted = true },
            { denied = it }
        )
        permissionHelper.onCreate(mockk())
        permissionHelper.launch()

        assert(granted.not()) { "Granted" }
        assert(setOf(Manifest.permission.ACCESS_FINE_LOCATION) == denied) { "Denied not valid" }
    }
}