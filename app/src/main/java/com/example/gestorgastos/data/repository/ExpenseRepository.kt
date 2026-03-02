package com.example.gestorgastos.data.repository

import com.example.gestorgastos.data.local.dao.ExpenseDao
import com.example.gestorgastos.data.local.dao.ExpenseListRow
import com.example.gestorgastos.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(
    private val expenseDao: ExpenseDao
) {
    fun observeListRows(fromDay: Int, toDay: Int): Flow<List<ExpenseListRow>> =
        expenseDao.observeListRows(fromDay, toDay)

    suspend fun insert(expense: ExpenseEntity): Long = expenseDao.insert(expense)

    suspend fun delete(expense: ExpenseEntity) = expenseDao.delete(expense)
}