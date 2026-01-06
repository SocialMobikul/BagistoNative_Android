package com.masilotti.demo.utils

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

// Add this to ImageSearchScreen.kt or a new file
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
                // Image option
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

                // Text option
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