package com.example.gestorgastos.data.repository

import com.example.gestorgastos.data.local.dao.CategoryDao
import com.example.gestorgastos.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

class CategoryRepository(
    private val categoryDao: CategoryDao
) {
    fun observeActive(): Flow<List<CategoryEntity>> = categoryDao.observeActive()

    suspend fun insert(name: String): Long = categoryDao.insert(CategoryEntity(name = name))

    suspend fun update(category: CategoryEntity) = categoryDao.update(category)

    suspend fun delete(category: CategoryEntity) = categoryDao.delete(category)
}