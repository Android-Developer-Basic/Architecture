package ru.otus.architecture

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import ru.otus.architecture.databinding.ActivitySettingsBinding
import ru.otus.architecture.stat.PageEventHelper

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
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.appBarLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                top = systemBars.top,
                left = systemBars.left,
                right = systemBars.right
            )
            insets
        }

        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }

        lifecycle.addObserver(PageEventHelper((application as App).statService))
    }
}