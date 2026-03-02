package com.example.gestorgastos.ui
import com.example.gestorgastos.data.repository.CategoryRepository
import com.example.gestorgastos.ui.categories.CategoriesViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gestorgastos.data.repository.ExpenseRepository
import com.example.gestorgastos.ui.add.AddExpenseViewModel
import com.example.gestorgastos.ui.list.ExpensesListViewModel
import com.example.gestorgastos.ui.edit.EditExpenseViewModel


class ExpensesListVMFactory(
    private val expenseRepo: ExpenseRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpensesListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpensesListViewModel(expenseRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class AddExpenseVMFactory(
    private val expenseRepo: ExpenseRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddExpenseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddExpenseViewModel(expenseRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class CategoriesVMFactory(
    private val categoryRepo: CategoryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoriesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CategoriesViewModel(categoryRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


class EditExpenseVMFactory(
    private val expenseRepo: ExpenseRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditExpenseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditExpenseViewModel(expenseRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}