package com.mobikul.bagisto.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Dialog for selecting scan type (image or text search).
 * 
 * This dialog presents the user with options to choose between
 * image-based search and text-based search when using the scanner.
 * 
 * Features:
 * - Image search option
 * - Text search option
 * - Cancel action
 * 
 * @param onImageSelected Callback when user selects image search
 * @param onTextSelected Callback when user selects text search
 * @param onDismiss Callback when dialog is dismissed
 * 
 * @see ImageSearchComponent
 * @see ImageSearchScreen
 * @see TextSearchScreen
 * 
 * @Composable
 */
@Composable
fun ScanTypeSelectionDialog(
    onImageSelected: () -> Unit,
    onTextSelected: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Search by scanning") },
        text = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onImageSelected() }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Image search",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Image")
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTextSelected() }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.TextFields,
                        contentDescription = "Text search",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Text")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
