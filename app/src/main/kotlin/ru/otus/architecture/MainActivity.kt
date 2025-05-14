package ru.otus.architecture

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import ru.otus.architecture.databinding.ActivityMainBinding
import ru.otus.architecture.permissions.PermissionHelper
import ru.otus.architecture.permissions.goToSettings
import ru.otus.architecture.stat.PageEventHelper

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeMenu()
        binding.btnSettings.setOnClickListener { goToSettings() }

        lifecycle.addObserver(PageEventHelper((application as App).statService))
        lifecycle.addObserver(
            PermissionHelper(
                this,
                activityResultRegistry,
                setOf(android.Manifest.permission.CAMERA),
                onGranted = { showCamera() },
                onDenied = {
                    with(binding) {
                        content.isVisible = false
                        permissionRequest.isVisible = true
                    }
                }
            )
        )
    }

    private fun initializeMenu() = with(binding) {
        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.item_settings -> {
                    startActivity(SettingsActivity.createIntent(this@MainActivity))
                    true
                }
                else -> false
            }
        }
    }

    private fun showCamera() = with(binding) {
        content.isVisible = true
        permissionRequest.isVisible = false
    }
}