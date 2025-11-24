package com.amijul.photowidget.presentation.editor


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.amijul.photowidget.widget.domain.PhotoWidgetMode
import com.amijul.photowidget.widget.ui.PhotoWidgetUiState
import com.amijul.photowidget.widget.ui.PhotoWidgetViewModel
import org.koin.androidx.compose.koinViewModel


/**
 * Entry point of the Note Widget Editor.
 * This composable is called by MainActivity.
 *
 * @param initialMode The mode requested by widget tap (PHOTO or TEXT).
 * @param vm The ViewModel.
 *        If using DI (Koin/Hilt) provide it externally.
 */
@Composable
fun PhotoWidgetEditorRoot(
    initialMode: PhotoWidgetMode,
    vm: PhotoWidgetViewModel = koinViewModel()
) {
    val uiState by vm.uiState.collectAsState()

    // When editor opens, we override the mode to whatever widget requested.
    LaunchedEffect(initialMode) {
        vm.setMode(initialMode)
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            uiState.isLoading -> EditorLoading()
            uiState.errorMessage != null -> EditorError(
                message = uiState.errorMessage!!,
                onRetry = { /* you may add retry behavior later */ }
            )
            else -> EditorContent(uiState, vm)
        }
    }
}

@Composable
private fun EditorLoading() {
    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EditorError(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Error: $message", color = Color.Red)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onRetry) { Text("Retry") }
        }
    }
}

/**
 * Editor content with tab bar, body, and save bar.
 */
@Composable
private fun EditorContent(
    ui: PhotoWidgetUiState,
    vm: PhotoWidgetViewModel
) {
    Column(Modifier.fillMaxSize()) {

        // ---- TOP TAB BAR ----
        EditorTabBar(
            mode = ui.state.mode,
            onSelectPhoto = { vm.setMode(PhotoWidgetMode.PHOTO) },
            onSelectText = { vm.setMode(PhotoWidgetMode.TEXT) }
        )

        // ---- BODY ----
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (ui.state.mode) {
                PhotoWidgetMode.PHOTO -> PhotoEditorScreen(
                    state = ui.state.photo,
                    onUriChange = vm::setPhotoUri,
                    onTransformChange = vm::updatePhotoTransform,
                    onReset = vm::resetPhotoTransform
                )
                PhotoWidgetMode.TEXT -> TextEditorScreen(
                    state = ui.state.text,
                    onTextChange = vm::updateTextContent,
                    onStyleChange = vm::updateTextStyle,
                    onReset = vm::resetText
                )
            }
        }

        // ---- SAVE BAR ----
        SaveBar(
            isSaving = ui.isSaving,
            onSave = vm::save
        )
    }
}

// ------------------------------------------------------
// SIMPLE COMPONENTS
// (We will build nice premium UI later. For now: structure.)
// ------------------------------------------------------

@Composable
private fun EditorTabBar(
    mode: PhotoWidgetMode,
    onSelectPhoto: () -> Unit,
    onSelectText: () -> Unit
) {
    TabRow(
        selectedTabIndex = if (mode == PhotoWidgetMode.PHOTO) 0 else 1,
        modifier = Modifier.fillMaxWidth()
    ) {
        Tab(
            selected = mode == PhotoWidgetMode.PHOTO,
            onClick = onSelectPhoto,
            text = { Text("Photo") }
        )
        Tab(
            selected = mode == PhotoWidgetMode.TEXT,
            onClick = onSelectText,
            text = { Text("Text") }
        )
    }
}

@Composable
private fun SaveBar(
    isSaving: Boolean,
    onSave: () -> Unit
) {
    Surface(shadowElevation = 8.dp) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Button(
                onClick = onSave,
                modifier = Modifier.align(Alignment.CenterEnd),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Save")
                }
            }
        }
    }
}
