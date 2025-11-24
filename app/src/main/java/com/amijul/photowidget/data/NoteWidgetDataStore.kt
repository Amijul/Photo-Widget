package com.amijul.photowidget.data


import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

// Single DataStore for note widget state
val Context.photoWidgetDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "note_widget_prefs"
)
