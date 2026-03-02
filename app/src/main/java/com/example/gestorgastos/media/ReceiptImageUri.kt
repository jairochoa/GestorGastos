package com.example.gestorgastos.media

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

fun createReceiptImageUri(context: Context): Uri {
    val dir = File(context.filesDir, "receipts").apply { mkdirs() }
    val file = File(dir, "receipt_${System.currentTimeMillis()}.jpg")

    // Debe coincidir con el authority del Manifest: ${applicationId}.fileprovider
    val authority = "${context.packageName}.fileprovider"

    return FileProvider.getUriForFile(context, authority, file)
}