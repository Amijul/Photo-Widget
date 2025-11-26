package com.amijul.photowidget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.lifecycleScope
import com.amijul.photowidget.presentation.editor.PhotoWidgetEditorRoot
import com.amijul.photowidget.ui.theme.PhotoWidgetTheme
import com.amijul.photowidget.widget.data.PhotoWidgetGlance
import com.amijul.photowidget.widget.domain.PhotoWidgetMode
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    companion object {
        const val EXTRA_OPEN_MODE = "extra_open_mode"
        private const val VALUE_MODE_PHOTO = "photo"
        private const val VALUE_MODE_TEXT = "text"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Decide which tab to open (PHOTO / TEXT)
        val initialMode = when (intent.getStringExtra(EXTRA_OPEN_MODE)) {
            VALUE_MODE_TEXT -> PhotoWidgetMode.TEXT
            VALUE_MODE_PHOTO -> PhotoWidgetMode.PHOTO
            else -> PhotoWidgetMode.PHOTO   // default when launching from launcher
        }

        enableEdgeToEdge()

        setContent {
            PhotoWidgetTheme {
                PhotoWidgetEditorRoot(
                    initialMode = initialMode,
                    onSaved = {
                        lifecycleScope.launch {
                            PhotoWidgetGlance().updateAll(this@MainActivity)
                            finish()
                        }
                    }
                )
            }
        }

    }
}
