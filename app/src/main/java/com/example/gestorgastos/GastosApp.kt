package com.example.gestorgastos

import android.app.Application
import com.example.gestorgastos.data.local.db.AppDatabase
import com.example.gestorgastos.data.repository.CategoryRepository
import com.example.gestorgastos.data.repository.ExpenseRepository
import com.example.gestorgastos.security.SecurityManager

class GastosApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

class AppContainer(app: Application) {
    private val db = AppDatabase.get(app)

    val expenseRepository = ExpenseRepository(db.expenseDao())
    val categoryRepository = CategoryRepository(db.categoryDao())
    val securityManager = SecurityManager(app)
}