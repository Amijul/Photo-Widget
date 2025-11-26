package com.amijul.photowidget.widget.ui


import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.Button
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.amijul.photowidget.MainActivity
import com.amijul.photowidget.widget.data.PhotoWidgetToggleModeAction
import com.amijul.photowidget.widget.domain.PhotoWidgetMode
import com.amijul.photowidget.widget.domain.PhotoWidgetState


@Composable
fun PhotoWidgetGlanceContent(
    context: Context,
    state: PhotoWidgetState,
    photoImage: ImageProvider?
)  {
    val isPhotoMode = state.mode == PhotoWidgetMode.PHOTO

    val openIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra(
            MainActivity.EXTRA_OPEN_MODE,
            if (isPhotoMode) "photo" else "text"
        )
    }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .cornerRadius(24.dp)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            // Top label
            Text(
                text = if (isPhotoMode) "Photo note" else "Text note",
                style = TextStyle(
                    color = GlanceTheme.colors.onBackground,
                    textAlign = TextAlign.Center
                ),
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )

            // Main content preview
            if (isPhotoMode) {
                if (photoImage != null) {
                    // Show actual photo thumbnail
                    Image(
                        provider = photoImage,
                        contentDescription = "Note photo",
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .cornerRadius(16.dp)
                            .padding(vertical = 4.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Fallback when no photo is set
                    Text(
                        text = "No photo",
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        ),
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                }
            } else {
                val previewText = if (state.text.content.isBlank()) {
                    "(Empty note)"
                } else {
                    state.text.content.take(40) + if (state.text.content.length > 40) "…" else ""
                }

                Text(
                    text = previewText,
                    style = TextStyle(
                        color = GlanceTheme.colors.onBackground,
                        textAlign = TextAlign.Center
                    ),
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }


            // Buttons row: Toggle + Edit
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Button(
                    text = if (isPhotoMode) "Show text" else "Show photo",
                    onClick = actionRunCallback<PhotoWidgetToggleModeAction>()
                )

                Button(
                    text = "Edit",
                    onClick = actionStartActivity(openIntent),
                    modifier = GlanceModifier.padding(start = 4.dp)
                )
            }
        }
    }
}
