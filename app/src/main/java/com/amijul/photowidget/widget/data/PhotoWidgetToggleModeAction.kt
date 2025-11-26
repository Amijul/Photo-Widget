package com.amijul.photowidget.widget.data

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.amijul.photowidget.data.photoWidgetDataStore    // ✅ new import
import com.amijul.photowidget.widget.domain.PhotoWidgetMode

class PhotoWidgetToggleModeAction : ActionCallback {      // ❌ no KoinComponent

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        // ✅ Build repo from Context + DataStore
        val repo = PhotoWidgetRepositoryImpl(appContext = context , dataStore =  context.photoWidgetDataStore)

        val current = repo.getState()
        val newMode = when (current.mode) {
            PhotoWidgetMode.PHOTO -> PhotoWidgetMode.TEXT
            PhotoWidgetMode.TEXT -> PhotoWidgetMode.PHOTO
        }

        repo.saveState(current.copy(mode = newMode))

        // ✅ force widget re-render
        PhotoWidgetGlance().update(context, glanceId)
    }
}
