package com.example.gestorgastos.data.local.dao

import androidx.room.*
import com.example.gestorgastos.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow
import androidx.room.ColumnInfo
import androidx.room.Embedded


data class ExpenseListRow(
    @Embedded val expense: ExpenseEntity,
    @ColumnInfo(name = "categoryName") val categoryName: String?
)


data class DailyTotal(
    val dateEpochDay: Int,
    val currency: String,
    val totalMinor: Long
)

data class CategoryTotal(
    val categoryId: Long?,
    val categoryName: String?,
    val currency: String,
    val totalMinor: Long
)

@Dao
interface ExpenseDao {

    @Query("""
        SELECT * FROM expenses
        WHERE dateEpochDay BETWEEN :fromDay AND :toDay
        ORDER BY dateEpochDay DESC, id DESC
    """)
    fun observeForRange(fromDay: Int, toDay: Int): Flow<List<ExpenseEntity>>

    @Insert
    suspend fun insert(expense: ExpenseEntity): Long

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Delete
    suspend fun delete(expense: ExpenseEntity)

    @Query("""
        SELECT dateEpochDay, currency, SUM(amountMinor) as totalMinor
        FROM expenses
        WHERE dateEpochDay BETWEEN :fromDay AND :toDay
        GROUP BY dateEpochDay, currency
        ORDER BY dateEpochDay ASC
    """)
    suspend fun dailyTotals(fromDay: Int, toDay: Int): List<DailyTotal>

    @Query("""
        SELECT e.categoryId as categoryId, c.name as categoryName, e.currency as currency,
               SUM(e.amountMinor) as totalMinor
        FROM expenses e
        LEFT JOIN categories c ON c.id = e.categoryId
        WHERE e.dateEpochDay BETWEEN :fromDay AND :toDay
        GROUP BY e.categoryId, c.name, e.currency
        ORDER BY totalMinor DESC
    """)
    suspend fun totalsByCategory(fromDay: Int, toDay: Int): List<CategoryTotal>

    @Query("""
    SELECT e.*, c.name AS categoryName
    FROM expenses e
    LEFT JOIN categories c ON c.id = e.categoryId
    WHERE e.dateEpochDay BETWEEN :fromDay AND :toDay
    ORDER BY e.dateEpochDay DESC, e.id DESC
""")
    fun observeListRows(fromDay: Int, toDay: Int): kotlinx.coroutines.flow.Flow<List<ExpenseListRow>>
}