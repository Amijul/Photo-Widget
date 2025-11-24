package com.amijul.photowidget


import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.amijul.photowidget.data.photoWidgetDataStore
import com.amijul.photowidget.widget.data.PhotoWidgetRepositoryImpl
import com.amijul.photowidget.widget.domain.PhotoWidgetRepository
import com.amijul.photowidget.widget.ui.PhotoWidgetViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    // Provide DataStore<Preferences> using the Context extension
    single<DataStore<Preferences>> {
        androidContext().photoWidgetDataStore
    }

    // Bind repository interface to implementation
    single<PhotoWidgetRepository> {
        PhotoWidgetRepositoryImpl(get())
    }

    // ViewModel using repository
    viewModel {
        PhotoWidgetViewModel(get())
    }
}
