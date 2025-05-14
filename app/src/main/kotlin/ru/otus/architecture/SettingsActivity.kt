package ru.otus.architecture

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.otus.architecture.databinding.ActivitySettingsBinding
import ru.otus.architecture.stat.createPageEvent

class SettingsActivity : AppCompatActivity() {
    companion object {
        fun createIntent(context: Context) = android.content.Intent(
            context,
            SettingsActivity::class.java
        )
    }

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }

        (application as App).statService.logEvent(createPageEvent("onCreate"))
    }

    override fun onStart() {
        super.onStart()
        (application as App).statService.logEvent(createPageEvent("onStart"))
    }

    override fun onResume() {
        super.onResume()
        (application as App).statService.logEvent(createPageEvent("onResume"))
    }

    override fun onPause() {
        super.onPause()
        (application as App).statService.logEvent(createPageEvent("onPause"))
    }

    override fun onStop() {
        super.onStop()
        (application as App).statService.logEvent(createPageEvent("onStop"))
    }

    override fun onDestroy() {
        super.onDestroy()
        (application as App).statService.logEvent(createPageEvent("onDestroy"))
    }
}