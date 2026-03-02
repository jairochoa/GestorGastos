package com.example.gestorgastos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("dateEpochDay"), Index("categoryId")]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    // Guardamos el monto en "minor units": centavos / unidades según moneda
    val amountMinor: Long,
    val currency: String, // "USD" | "COP" | "VES"

    val concept: String,
    val description: String? = null,
    val merchant: String? = null,
    val address: String? = null,
    val paymentMethod: String, // enum name: CASH, DEBIT...

    val categoryId: Long? = null,

    // Fecha como epochDay para filtrar rápido (LocalDate.toEpochDay().toInt())
    val dateEpochDay: Int,

    // Recibo: guardamos el URI como String (content://...)
    val receiptUri: String? = null,

    val createdAtEpochMillis: Long = System.currentTimeMillis(),
    val updatedAtEpochMillis: Long = System.currentTimeMillis()
)