package com.amijul.photowidget.widget.data

import android.content.Context
import android.graphics.BitmapFactory
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import com.amijul.photowidget.data.photoWidgetDataStore
import com.amijul.photowidget.widget.ui.PhotoWidgetGlanceContent

class PhotoWidgetGlance : GlanceAppWidget() {

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {
        // Use repo built directly from DataStore + app context
        val repo = PhotoWidgetRepositoryImpl(
            dataStore = context.photoWidgetDataStore,
            appContext = context.applicationContext
        )
        val state = repo.getState()

        // Decode from local file path (not content://)
        val photoImage: ImageProvider? = state.photo.photoUri?.let { path ->
            runCatching {
                val bitmap = BitmapFactory.decodeFile(path)
                bitmap?.let { ImageProvider(it) }
            }.getOrNull()
        }

        provideContent {
            GlanceTheme {
                PhotoWidgetGlanceContent(
                    context = context,
                    state = state,
                    photoImage = photoImage
                )
            }
        }
    }
}
