package com.example.gestorgastos.media

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * Copia un Uri (picker) a almacenamiento interno y devuelve un Uri estable via FileProvider.
 * Para cámara NO hace falta: ya generas un Uri interno con createReceiptImageUri().
 */
fun copyReceiptToInternal(context: Context, source: Uri): Uri {
    val dir = File(context.filesDir, "receipts").apply { mkdirs() }
    val outFile = File(dir, "receipt_${System.currentTimeMillis()}.jpg")

    context.contentResolver.openInputStream(source)?.use { input ->
        outFile.outputStream().use { output ->
            input.copyTo(output)
        }
    } ?: error("No se pudo leer el Uri del recibo")

    val authority = "${context.packageName}.fileprovider"
    return FileProvider.getUriForFile(context, authority, outFile)
}

fun openReceiptImage(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "image/*")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(intent)
}