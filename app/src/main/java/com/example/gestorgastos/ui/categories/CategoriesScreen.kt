@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.gestorgastos.ui.categories

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gestorgastos.data.local.entity.CategoryEntity

@Composable
fun CategoriesScreen(
    categories: List<CategoryEntity>,
    onBack: () -> Unit,
    onAdd: (String) -> Unit,
    onRename: (CategoryEntity, String) -> Unit,
    onDelete: (CategoryEntity) -> Unit
) {
    var newName by remember { mutableStateOf("") }

    var editTarget by remember { mutableStateOf<CategoryEntity?>(null) }
    var editName by remember { mutableStateOf("") }

    var deleteTarget by remember { mutableStateOf<CategoryEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categorías") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Volver") } }
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Nueva categoría") },
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = { onAdd(newName); newName = "" }) {
                    Text("Agregar")
                }
            }

            Divider()

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories, key = { it.id }) { c ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(c.name, style = MaterialTheme.typography.titleMedium)

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = {
                                    editTarget = c
                                    editName = c.name
                                }) { Text("Editar") }

                                TextButton(onClick = { deleteTarget = c }) { Text("Borrar") }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog editar
    if (editTarget != null) {
        AlertDialog(
            onDismissRequest = { editTarget = null },
            confirmButton = {
                TextButton(onClick = {
                    val target = editTarget!!
                    onRename(target, editName)
                    editTarget = null
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { editTarget = null }) { Text("Cancelar") }
            },
            title = { Text("Editar categoría") },
            text = {
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
    }

    // Dialog borrar
    if (deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(deleteTarget!!)
                    deleteTarget = null
                }) { Text("Sí, borrar") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancelar") }
            },
            title = { Text("Borrar categoría") },
            text = { Text("Esto no borra gastos; solo les quitará la categoría.") }
        )
    }
}