package ru.otus.architecture

import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.LifecycleCameraController
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import ru.otus.architecture.databinding.ActivityMainBinding
import ru.otus.architecture.permissions.PermissionHelper
import ru.otus.architecture.permissions.goToSettings
import ru.otus.architecture.stat.PageEventHelper
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var capturedBehavior: BottomSheetBehavior<ConstraintLayout>

    private var content: String by Delegates.observable("") { _, _, newValue ->
        binding.text.text = newValue.takeIf { it.isNotBlank() } ?: getString(R.string.text_scan_something)
        binding.share.isEnabled = newValue.isNotBlank()
    }
    private var detectedText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeMenu()
        initializeContent()
        setupWindowInsets()
        binding.btnSettings.setOnClickListener { goToSettings() }
        binding.plus.setOnClickListener {
            content += detectedText
        }
        binding.share.setOnClickListener {
            startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, content)
            }, null))
        }

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
                        capturedBehavior.isHideable = true
                        capturedBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                        plus.hide()
                    }
                }
            )
        )
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.appBarLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                top = systemBars.top,
                left = systemBars.left,
                right = systemBars.right
            )
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.permissionRequest) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                left = systemBars.left,
                right = systemBars.right,
                bottom = systemBars.bottom
            )
            insets
        }

        val initialPeekHeight = resources.getDimensionPixelSize(R.dimen.bottom_sheet_collapsed)
        ViewCompat.setOnApplyWindowInsetsListener(binding.captured) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                left = systemBars.left,
                right = systemBars.right,
                bottom = systemBars.bottom
            )
            capturedBehavior.peekHeight = initialPeekHeight + systemBars.bottom
            insets
        }

        val initialFabMarginBottom = resources.getDimensionPixelSize(R.dimen.bottom_sheet_fab)
        val initialFabMarginEnd = resources.getDimensionPixelSize(R.dimen.horizontal_margin)
        ViewCompat.setOnApplyWindowInsetsListener(binding.plus) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = initialFabMarginBottom + systemBars.bottom
                rightMargin = initialFabMarginEnd + systemBars.right
            }
            insets
        }
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

    private fun initializeContent() {
        capturedBehavior = BottomSheetBehavior.from(binding.captured)
        capturedBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // Do nothing
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        binding.plus.hide()
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        binding.plus.show()
                    }
                }
            }
        })
    }

    private fun showCamera() {
        with(binding) {
            content.isVisible = true
            permissionRequest.isVisible = false
            capturedBehavior.isHideable = false
            capturedBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            plus.show()
        }
        val executor = ContextCompat.getMainExecutor(this)
        val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        binding.viewFinder.controller = LifecycleCameraController(this).apply {
            setImageAnalysisAnalyzer(
                executor,
                MlKitAnalyzer(
                    listOf(textRecognizer),
                    COORDINATE_SYSTEM_VIEW_REFERENCED,
                    executor
                ) { analyzerResult ->
                    analyzerResult.getValue(textRecognizer)?.let { result ->
                        binding.overlay.rectBounds = result.textBlocks
                            .takeIf { it.isNotEmpty() }
                            ?.let {
                                listOf(
                                    Rect(
                                        result.textBlocks.mapNotNull { it.boundingBox?.left }.min(),
                                        result.textBlocks.mapNotNull { it.boundingBox?.top }.min(),
                                        result.textBlocks.mapNotNull { it.boundingBox?.right }.max(),
                                        result.textBlocks.mapNotNull { it.boundingBox?.bottom }.max()
                                    )
                                )
                            }
                            ?: emptyList()

                        detectedText = result.text
                        binding.plus.isEnabled = result.text.isNotBlank()
                    }
                }
            )
            bindToLifecycle(this@MainActivity)
        }
    }
}