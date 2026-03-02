@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.gestorgastos.ui.receipt

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ReceiptThumbnail(
    uri: Uri,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val img = produceState(initialValue = null as android.graphics.Bitmap?, key1 = uri) {
        value = withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    // Decodificación simple; si luego quieres, lo optimizamos con inSampleSize.
                    BitmapFactory.decodeStream(input)
                }
            } catch (_: Throwable) {
                null
            }
        }
    }.value

    Column(modifier) {
        Text("Recibo", style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(6.dp))

        Surface(
            modifier = Modifier
                .size(140.dp)
                .clickable { onClick() }
        ) {
            if (img != null) {
                Image(
                    bitmap = img.asImageBitmap(),
                    contentDescription = "Recibo",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("Sin vista previa")
                }
            }
        }
    }
}