package com.amijul.photowidget.widget.data

import com.amijul.photowidget.widget.domain.PhotoWidgetRepository
import com.amijul.photowidget.widget.domain.PhotoWidgetState
import kotlinx.coroutines.flow.Flow
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class PhotoWidgetRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
    private val json: Json = defaultJson
) : PhotoWidgetRepository {

    companion object {
        private val KEY_STATE_JSON = stringPreferencesKey("photo_widget_state")

        val defaultJson: Json = Json{
            ignoreUnknownKeys = true
            prettyPrint = false
            encodeDefaults = true
        }
    }

    override val stateFlow: Flow<PhotoWidgetState> =
        dataStore.data.map{ prefs ->
            val raw = prefs[KEY_STATE_JSON]

            if(raw.isNullOrBlank()) {
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
        val encode = json.encodeToString(state)
        dataStore.edit { prefs ->
            prefs[KEY_STATE_JSON] = encode
        }
    }
}