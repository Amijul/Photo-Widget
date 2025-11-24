package com.amijul.photowidget.widget.domain

import kotlinx.coroutines.flow.Flow

interface PhotoWidgetRepository {

    val stateFlow: Flow<PhotoWidgetState>

    suspend fun getState(): PhotoWidgetState

    suspend fun saveState(state: PhotoWidgetState)
}