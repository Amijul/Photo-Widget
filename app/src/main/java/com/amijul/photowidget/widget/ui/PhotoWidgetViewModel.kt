package com.amijul.photowidget.widget.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amijul.photowidget.widget.domain.CropRect
import com.amijul.photowidget.widget.domain.PhotoState
import com.amijul.photowidget.widget.domain.PhotoWidgetMode
import com.amijul.photowidget.widget.domain.PhotoWidgetRepository
import com.amijul.photowidget.widget.domain.PhotoWidgetState
import com.amijul.photowidget.widget.domain.TextAlignment
import com.amijul.photowidget.widget.domain.TextState
import com.amijul.photowidget.widget.domain.TextStyleState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class PhotoWidgetUiState(
    val state: PhotoWidgetState = PhotoWidgetState(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)


class PhotoWidgetViewModel(
    private val repo: PhotoWidgetRepository
): ViewModel() {

    private val _uiState = MutableStateFlow(PhotoWidgetUiState())
    val uiState: StateFlow<PhotoWidgetUiState> = _uiState.asStateFlow()


    init {

        viewModelScope.launch {
            try {

                val initial = repo.getState()

                _uiState.update {
                    it.copy(
                        state = initial,
                        isLoading = false,
                        errorMessage = null,
                    )
                }

            } catch (t: Throwable){
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = t.message ?: "Failed to load note"
                    )
                }
            }
        }
    }




    // ---------- Core helpers ----------

    private inline fun updateEditingState(
        crossinline transform: (PhotoWidgetState) -> PhotoWidgetState
    ){

        _uiState.update { current ->
            current.copy(
                state = transform(current.state),
                errorMessage = null
            )
        }

    }

    fun toggleMode() {
        updateEditingState { current ->
            val newMode = when (current.mode) {
                PhotoWidgetMode.PHOTO -> PhotoWidgetMode.PHOTO
                PhotoWidgetMode.TEXT ->  PhotoWidgetMode.TEXT
            }
            current.copy(mode = newMode)

        }
    }

    fun setMode(mode: PhotoWidgetMode) {
        updateEditingState {
            it.copy(mode = mode)
        }
    }


    // ---------- Photo editing ----------

    fun setPhotoUri(uriString: String?) {
        updateEditingState { current ->
            val newPhoto = current.photo.copy(photoUri = uriString)
            current.copy(photo = newPhoto)
        }
    }

    fun updatePhotoTransform(
        zoom: Float? = null,
        offsetX: Float? = null,
        offsetY: Float? = null,
        brightness: Float? = null,
        crop: CropRect? = null
    ) {
        updateEditingState { current ->
            val old = current.photo
            val new = old.copy(
                zoom = zoom?.let { clamp(it, 0.5f, 4f) } ?: old.zoom,
                offsetX = offsetX ?: old.offsetX,
                offsetY = offsetY ?: old.offsetY,
                brightness = brightness?.let { clamp(it, -1f, 1f) } ?: old.brightness,
                crop = crop ?: old.crop
            )
            current.copy(photo = new)
        }
    }

    fun resetPhotoTransform() {
        updateEditingState { current ->
            current.copy(photo = PhotoState(photoUri = current.photo.photoUri))
        }
    }

    // ---------- Text content ----------

    fun updateTextContent(content: String) {
        updateEditingState { current ->
            val newText = current.text.copy(content = content)
            current.copy(text = newText)
        }
    }

    fun updateTextStyle(
        fontFamilyKey: String? = null,
        isBold: Boolean? = null,
        alignment: TextAlignment? = null,
        fontSizeSp: Float? = null
    ) {
        updateEditingState { current ->
            val old = current.text.style
            val newStyle = old.copy(
                fontFamilyKey = fontFamilyKey ?: old.fontFamilyKey,
                isBold = isBold ?: old.isBold,
                alignment = alignment ?: old.alignment,
                fontSizeSp = fontSizeSp ?: old.fontSizeSp
            )
            current.copy(text = current.text.copy(style = newStyle))
        }
    }

    fun resetText() {
        updateEditingState { current ->
            current.copy(
                text = TextState(
                    content = "",
                    style = TextStyleState()
                )
            )
        }
    }

    // ---------- Save ----------

    /**
     * Persists the current editing state to the repository.
     * UI can observe [isSaving] to show a loading indicator and
     * close the editor on successful completion.
     */
    fun save() {
        val currentState = _uiState.value.state

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            try {
                repo.saveState(currentState)
                _uiState.update { it.copy(isSaving = false) }
                // Activity/Screen decides when to close after observing isSaving -> false
            } catch (t: Throwable) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = t.message ?: "Failed to save note"
                    )
                }
            }
        }
    }

    // ---------- Util ----------

    private fun clamp(value: Float, min: Float, max: Float): Float {
        return when {
            value < min -> min
            value > max -> max
            else -> value
        }
    }

}







