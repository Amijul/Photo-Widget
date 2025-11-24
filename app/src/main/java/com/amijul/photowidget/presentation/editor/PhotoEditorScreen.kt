package com.amijul.photowidget.presentation.editor


import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import androidx.compose.ui.graphics.ColorMatrix
import coil3.compose.AsyncImage
import com.amijul.photowidget.widget.domain.CropRect
import com.amijul.photowidget.widget.domain.PhotoState

/**
 * Photo editor surface shown when mode = PHOTO.
 *
 * @param state Current photo state from ViewModel.
 * @param onUriChange Called when user picks or clears a photo (string-form Uri or null).
 * @param onTransformChange Called whenever zoom / pan / brightness (or crop) changes.
 * @param onReset Called when user presses Reset to restore default transform.
 */
@Composable
fun PhotoEditorScreen(
    state: PhotoState,
    onUriChange: (String?) -> Unit,
    onTransformChange: (
        zoom: Float?,
        offsetX: Float?,
        offsetY: Float?,
        brightness: Float?,
        crop: CropRect?
    ) -> Unit,
    onReset: () -> Unit
) {
    // Local editable transform state, driven by domain state but updated by gestures.
    var zoom by remember(state.photoUri) { mutableStateOf(state.zoom) }
    var offsetX by remember(state.photoUri) { mutableStateOf(state.offsetX) }
    var offsetY by remember(state.photoUri) { mutableStateOf(state.offsetY) }
    var brightness by remember(state.photoUri) { mutableStateOf(state.brightness) }

    // Sync if domain state changes externally (e.g. reset from VM).
    LaunchedEffect(state.zoom, state.offsetX, state.offsetY, state.brightness, state.photoUri) {
        zoom = state.zoom
        offsetX = state.offsetX
        offsetY = state.offsetY
        brightness = state.brightness
    }

    // Gesture handler for pinch + pan
    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        val newZoom = (zoom * zoomChange).coerceIn(0.5f, 4f)
        zoom = newZoom
        offsetX += panChange.x
        offsetY += panChange.y

        onTransformChange(
            newZoom,   // zoom
            offsetX,   // offsetX
            offsetY,   // offsetY
            null,      // brightness
            null       // crop
        )

    }

    // System photo picker
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        val uriString = uri?.toString()
        onUriChange(uriString)
        // After new image set, ViewModel will update state and LaunchedEffect will sync local vars
    }

    // Background of the whole area
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            )
            .padding(16.dp)
    ) {

        // Top title / hint
        Text(
            text = "Photo",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Main photo card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                if (state.photoUri == null) {
                    // Placeholder when no image
                    EmptyPhotoPlaceholder(
                        onPickClick = {
                            pickImageLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )
                } else {
                    // Actual image with pinch & pan
                    val brightnessMatrix = remember(brightness) {
                        val t = (brightness * 255f).coerceIn(-255f, 255f)

                        ColorMatrix(
                            floatArrayOf(
                                1f, 0f, 0f, 0f, t,
                                0f, 1f, 0f, 0f, t,
                                0f, 0f, 1f, 0f, t,
                                0f, 0f, 0f, 1f, 0f
                            )
                        )
                    }


                    AsyncImage(
                        model = state.photoUri,
                        contentDescription = "Note photo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(24.dp))
                            .graphicsLayer {
                                scaleX = zoom
                                scaleY = zoom
                                translationX = offsetX
                                translationY = offsetY
                            }
                            .transformable(
                                state = transformableState,
                                lockRotationOnZoomPan = true
                            ),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        colorFilter = ColorFilter.colorMatrix(brightnessMatrix)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Controls card (brightness, pick, reset)
        ControlPanel(
            hasPhoto = state.photoUri != null,
            zoom = zoom,
            brightness = brightness,
            onBrightnessChange = { new ->
                brightness = new
                onTransformChange(
                     null,
                     null,
                     null,
                     new,
                     null
                )
            },
            onPickImage = {
                pickImageLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onReset = {
                onReset()
            }
        )
    }
}

@Composable
private fun EmptyPhotoPlaceholder(
    onPickClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Image,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Tap to choose a photo",
            color = Color.White.copy(alpha = 0.9f),
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onPickClick,
            shape = RoundedCornerShape(999.dp)
        ) {
            Text("Pick photo")
        }
    }
}

@Composable
private fun ControlPanel(
    hasPhoto: Boolean,
    zoom: Float,
    brightness: Float,
    onBrightnessChange: (Float) -> Unit,
    onPickImage: () -> Unit,
    onReset: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            // Zoom indicator (read-only – actual zoom from pinch)
            Text(
                text = "Zoom: x${(zoom * 10f).roundToInt() / 10f}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(12.dp))

            // Brightness slider
            Text(
                text = "Brightness",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Slider(
                value = brightness,
                onValueChange = onBrightnessChange,
                valueRange = -1f..1f,
                steps = 0,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Darker", style = MaterialTheme.typography.bodySmall)
                Text("Default", style = MaterialTheme.typography.bodySmall)
                Text("Brighter", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ElevatedButton(
                    onClick = onPickImage,
                    shape = RoundedCornerShape(999.dp)
                ) {
                    ButtonIconText(
                        icon = Icons.Default.Image,
                        label = if (hasPhoto) "Replace photo" else "Pick photo"
                    )
                }

                TextButton(
                    onClick = onReset,
                    enabled = hasPhoto
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset")
                }
            }
        }
    }
}

@Composable
private fun ButtonIconText(
    icon: ImageVector,
    label: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(label)
    }
}
