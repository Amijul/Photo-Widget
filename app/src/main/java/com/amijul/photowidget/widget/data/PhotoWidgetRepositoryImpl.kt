package com.amijul.photowidget.widget.data

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.amijul.photowidget.widget.domain.PhotoWidgetRepository
import com.amijul.photowidget.widget.domain.PhotoWidgetState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream

class PhotoWidgetRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
    private val appContext: Context,
    private val json: Json = defaultJson
) : PhotoWidgetRepository {

    companion object {
        private val KEY_STATE_JSON = stringPreferencesKey("photo_widget_state")

        val defaultJson: Json = Json {
            ignoreUnknownKeys = true
            prettyPrint = false
            encodeDefaults = true
        }
    }

    override val stateFlow: Flow<PhotoWidgetState> =
        dataStore.data.map { prefs ->
            val raw = prefs[KEY_STATE_JSON]

            if (raw.isNullOrBlank()) {
                PhotoWidgetState()
            } else {
                runCatching { json.decodeFromString<PhotoWidgetState>(raw) }
                    .getOrElse { PhotoWidgetState() }
            }
        }

    override suspend fun getState(): PhotoWidgetState {
        return stateFlow.first()
    }

    override suspend fun saveState(state: PhotoWidgetState) {
        // Ensure the photoUri is a local file path accessible to the widget
        val safePhotoPath = copyPhotoToInternal(state.photo.photoUri)

        val safeState = state.copy(
            photo = state.photo.copy(photoUri = safePhotoPath)
        )

        val encoded = json.encodeToString(safeState)
        dataStore.edit { prefs ->
            prefs[KEY_STATE_JSON] = encoded
        }
    }

    /**
     * Copies the given URI (content://, etc.) into internal storage and returns
     * the absolute file path. If it's already a file path ("/..."), it just returns it.
     */
    private suspend fun copyPhotoToInternal(uriString: String?): String? {
        if (uriString.isNullOrBlank()) return null

        // If it's already a file path, keep it
        if (uriString.startsWith("/")) return uriString

        return try {
            val uri = Uri.parse(uriString)
            val input = appContext.contentResolver.openInputStream(uri) ?: return null

            val dir = File(appContext.filesDir, "photo_widget")
            if (!dir.exists()) dir.mkdirs()

            val file = File(dir, "photo.jpg")

            input.use { inp ->
                FileOutputStream(file).use { out ->
                    inp.copyTo(out)
                }
            }

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
