package com.amijul.photowidget.widget.domain

import kotlinx.serialization.Serializable

enum class PhotoWidgetMode{  PHOTO, TEXT }


/**
 * Simple normalized crop rectangle.
 * 0f..1f coordinates relative to the full image.
 */
@Serializable
data class CropRect(
    val left: Float = 0f,
    val top: Float = 0f,
    val right: Float = 1f,
    val bottom: Float = 1f
)

/**
 * All parameters required to render the photo.
 */
@Serializable
data class PhotoState(
    val photoUri: String? = null, // String-form Uri; null = no image yet
    val zoom: Float = 1f,         // 1f = original size
    val offsetX: Float = 0f,      // normalized pan, e.g. -1f..+1f
    val offsetY: Float = 0f,
    val brightness: Float = 0f,   // -1f (darker) .. +1f (brighter), 0f = original
    val crop: CropRect? = null    // null = full image
)

/**
 * Text alignment inside the sticky note.
 */
@Serializable
enum class TextAlignment {
    LEFT,
    CENTER,
    RIGHT
}

/**
 * Typography style settings that are independent from actual font objects.
 */
@Serializable
data class TextStyleState(
    val fontFamilyKey: String = "default", // "default", "serif", "sans", etc.
    val isBold: Boolean = false,
    val alignment: TextAlignment = TextAlignment.CENTER,
    val fontSizeSp: Float = 16f
)

/**
 * Text content for the note widget.
 */
@Serializable
data class TextState(
    val content: String = "",
    val style: TextStyleState = TextStyleState()
)

/**
 * Complete widget state and single source of truth.
 */
@Serializable
data class PhotoWidgetState(
    val mode: PhotoWidgetMode = PhotoWidgetMode.PHOTO,
    val photo: PhotoState = PhotoState(),
    val text: TextState = TextState()
) {
    val hasPhoto: Boolean get() = photo.photoUri != null
    val hasText: Boolean get() = text.content.isNotBlank()
}












