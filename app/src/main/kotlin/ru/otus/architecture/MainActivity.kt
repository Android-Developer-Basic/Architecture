package ru.otus.architecture

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.otus.architecture.databinding.ActivityMainBinding
import ru.otus.architecture.stat.PageEventHelper

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeMenu()

        lifecycle.addObserver(PageEventHelper((application as App).statService))
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
}