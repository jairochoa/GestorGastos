package com.example.gestorgastos.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestorgastos.data.local.entity.CategoryEntity
import com.example.gestorgastos.data.repository.CategoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoriesViewModel(
    private val repo: CategoryRepository
) : ViewModel() {

    val categories: StateFlow<List<CategoryEntity>> =
        repo.observeActive().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun add(name: String) {
        val clean = name.trim()
        if (clean.isBlank()) return
        viewModelScope.launch { repo.insert(clean) }
    }

    fun rename(category: CategoryEntity, newName: String) {
        val clean = newName.trim()
        if (clean.isBlank()) return
        viewModelScope.launch { repo.update(category.copy(name = clean)) }
    }

    fun delete(category: CategoryEntity) {
        viewModelScope.launch { repo.delete(category) }
    }
}