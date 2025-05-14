package ru.otus.architecture

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.otus.architecture.databinding.ActivityMainBinding
import ru.otus.architecture.stat.createPageEvent

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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