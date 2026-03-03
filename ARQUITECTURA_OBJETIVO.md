# Arquitectura objetivo propuesta para GestorGastos

## Objetivo
Definir una arquitectura que permita evolucionar el MVP actual hacia una app mantenible y escalable **sin romper funcionalidades existentes** (alta/edición/listado, categorías, reportes, CSV, recibos y PIN).

---

## 1) Estructura de paquetes objetivo

> Estrategia: pasar de organización principalmente por capa técnica a una organización **por feature + capas internas**.

```text
com.example.gestorgastos
├─ app/
│  ├─ MainActivity.kt
│  ├─ AppNav.kt
│  └─ di/
│     ├─ AppModule.kt
│     └─ DatabaseModule.kt
│
├─ core/
│  ├─ common/
│  │  ├─ Result.kt
│  │  ├─ AppError.kt
│  │  └─ DispatcherProvider.kt
│  ├─ ui/
│  │  ├─ UiText.kt
│  │  └─ components/
│  ├─ time/
│  │  ├─ ClockProvider.kt
│  │  └─ DateUtils.kt
│  ├─ money/
│  │  ├─ MoneyParser.kt
│  │  └─ MoneyFormatter.kt
│  └─ security/
│     └─ PinHasher.kt
│
├─ feature/
│  ├─ expenses/
│  │  ├─ domain/
│  │  │  ├─ model/
│  │  │  │  ├─ Expense.kt
│  │  │  │  └─ ExpenseId.kt
│  │  │  ├─ repository/
│  │  │  │  └─ ExpenseRepository.kt
│  │  │  └─ usecase/
│  │  │     ├─ AddExpenseUseCase.kt
│  │  │     ├─ UpdateExpenseUseCase.kt
│  │  │     ├─ DeleteExpenseUseCase.kt
│  │  │     ├─ ObserveMonthlyExpensesUseCase.kt
│  │  │     ├─ ExportMonthlyCsvUseCase.kt
│  │  │     └─ ValidateExpenseInputUseCase.kt
│  │  ├─ data/
│  │  │  ├─ local/
│  │  │  │  ├─ dao/ExpenseDao.kt
│  │  │  │  ├─ entity/ExpenseEntity.kt
│  │  │  │  └─ mapper/ExpenseMapper.kt
│  │  │  └─ repository/ExpenseRepositoryImpl.kt
│  │  └─ presentation/
│  │     ├─ list/
│  │     │  ├─ ExpensesListViewModel.kt
│  │     │  └─ ExpensesListScreen.kt
│  │     ├─ add/
│  │     │  ├─ AddExpenseViewModel.kt
│  │     │  └─ AddExpenseScreen.kt
│  │     └─ edit/
│  │        ├─ EditExpenseViewModel.kt
│  │        └─ EditExpenseScreen.kt
│  │
│  ├─ categories/
│  │  ├─ domain/{model,repository,usecase}
│  │  ├─ data/{local,repository}
│  │  └─ presentation/
│  │
│  ├─ reports/
│  │  ├─ domain/{model,repository,usecase}
│  │  ├─ data/
│  │  └─ presentation/
│  │
│  ├─ security/
│  │  ├─ domain/{repository,usecase}
│  │  ├─ data/
│  │  └─ presentation/
│  │
│  └─ receipts/
│     ├─ domain/{repository,usecase}
│     ├─ data/
│     └─ presentation/
│
└─ data/local/db/
   └─ AppDatabase.kt
```

### Principios de esta estructura
- **Feature-first:** cada módulo funcional tiene todo lo necesario (domain/data/presentation).
- **Dependencias unidireccionales:** presentation -> domain -> data.
- **Core compartido mínimo:** utilidades transversales (errores, dinero, tiempo, UI base).
- **Migración gradual:** se puede mover feature por feature sin big-bang.

---

## 2) Contratos (interfaces) recomendados

## 2.1 Expense (dominio)
```kotlin
interface ExpenseRepository {
    fun observeMonthlyExpenses(month: YearMonth): Flow<List<Expense>>
    suspend fun getById(id: ExpenseId): Expense?
    suspend fun add(input: NewExpense): ExpenseId
    suspend fun update(input: UpdateExpense): Unit
    suspend fun delete(id: ExpenseId): Unit
}
```

## 2.2 Reports (dominio)
```kotlin
interface ReportsRepository {
    suspend fun getDailyTotals(month: YearMonth, currency: CurrencyCode): List<DailyTotal>
    suspend fun getCategoryTotals(month: YearMonth, currency: CurrencyCode): List<CategoryTotal>
}
```

## 2.3 Categories (dominio)
```kotlin
interface CategoryRepository {
    fun observeAll(): Flow<List<Category>>
    suspend fun add(name: String): CategoryId
    suspend fun rename(id: CategoryId, name: String)
    suspend fun delete(id: CategoryId)
}
```

## 2.4 Security (dominio)
```kotlin
interface PinRepository {
    fun isConfigured(): Boolean
    suspend fun set(pin: String)
    suspend fun verify(pin: String): Boolean
    suspend fun clear()
}
```

## 2.5 Receipts (dominio)
```kotlin
interface ReceiptRepository {
    suspend fun copyFromPicker(uri: Uri): Uri
    fun createCameraUri(): Uri
    fun open(uri: Uri)
}
```

### Convenciones de contrato
- En dominio, usar tipos ricos (`ExpenseId`, `CurrencyCode`) en lugar de primitivos donde aplique.
- Evitar `Boolean` para errores de negocio: usar `Result<Success, AppError>`.
- Las interfaces viven en `domain/repository`; implementaciones en `data/repository`.

---

## 3) Casos de uso concretos (MVP + crecimiento)

## Expenses
1. `ValidateExpenseInputUseCase`
   - Input: texto de monto, concepto, moneda, método de pago.
   - Output: `Result<ValidatedExpenseInput, ValidationError>`.

2. `AddExpenseUseCase`
   - Orquesta validación + mapeo + persistencia.
   - Output: `Result<ExpenseId, AppError>`.

3. `UpdateExpenseUseCase`
   - Misma lógica que add pero para edición.

4. `DeleteExpenseUseCase`
   - Elimina por ID.

5. `ObserveMonthlyExpensesUseCase`
   - Expone `Flow<List<ExpenseListItem>>` para el mes activo.

6. `ExportMonthlyCsvUseCase`
   - Recibe lista/mes y devuelve CSV serializado (sin acoplar a UI).

## Reports
7. `GetMonthlyReportUseCase`
   - Recibe mes + moneda y devuelve `ReportSnapshot(daily, byCategory, totals)`.

## Categories
8. `ObserveCategoriesUseCase`
9. `CreateCategoryUseCase`
10. `RenameCategoryUseCase`
11. `DeleteCategoryUseCase`

## Security
12. `IsPinConfiguredUseCase`
13. `SetPinUseCase`
14. `VerifyPinUseCase`
15. `DisablePinUseCase`

## Receipts
16. `AttachReceiptFromPickerUseCase`
17. `CreateReceiptCameraUriUseCase`
18. `OpenReceiptUseCase`

---

## 4) Modelo de estado y errores

## 4.1 Resultado estándar
```kotlin
sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Error(val error: AppError) : AppResult<Nothing>
}
```

## 4.2 Error de dominio
```kotlin
sealed interface AppError {
    data object Validation : AppError
    data object NotFound : AppError
    data object StorageFailure : AppError
    data object SecurityFailure : AppError
    data class Unknown(val cause: Throwable? = null) : AppError
}
```

## 4.3 Estado de pantalla
```kotlin
sealed interface UiState<out T> {
    data object Idle : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Data<T>(val value: T) : UiState<T>
    data class Error(val message: UiText) : UiState<Nothing>
}
```

**Beneficio:** ViewModel deja de operar con `Boolean` y mejora feedback al usuario.

---

## 5) Plan de migración incremental (sin romper lo actual)

## Fase 0 — Preparación (1-2 días)
- Crear paquete `core/common` con `AppResult`, `AppError` y `UiState`.
- Añadir tests unitarios a utilidades existentes de montos/fecha.
- Mantener todas las pantallas y DAOs como están.

**Riesgo:** bajo.

## Fase 1 — Expenses vertical slice (2-4 días)
- Crear `feature/expenses/domain` con `ExpenseRepository` (contrato) y casos de uso.
- Implementar `ExpenseRepositoryImpl` adaptando el repositorio actual.
- Refactor de `AddExpenseViewModel` y `EditExpenseViewModel` para usar casos de uso.
- Mantener `Screens` casi intactas (solo cambios de estado/errores).

**Resultado:** funcionalidad principal migrada sin cambiar UX.

## Fase 2 — Reports y CSV (2-3 días)
- Crear `GetMonthlyReportUseCase` y `ExportMonthlyCsvUseCase`.
- Sacar lógica de CSV de `AppNav` hacia use case en `feature/expenses/domain/usecase`.
- `ReportsViewModel` consume casos de uso en lugar de repositorio directo.

**Resultado:** navegación más liviana y negocio fuera de UI.

## Fase 3 — Categories y Security (2-4 días)
- Crear contratos/casos de uso para categorías y PIN.
- Envolver implementación actual de `SecurityManager` dentro de `PinRepositoryImpl`.
- Migrar ViewModels de categorías y lock/setup.

**Resultado:** uniformidad de arquitectura entre features.

## Fase 4 — DI y limpieza final (2-3 días)
- Migrar factories manuales a Hilt (o Koin si prefieres simpleza).
- Eliminar factories legacy y centralizar provisión de casos de uso.
- Homologar dependencias en `libs.versions.toml`.

**Resultado:** menor acoplamiento, más testable.

---

## 6) Criterios de “no ruptura” por fase

Antes de cerrar cada fase, validar:
1. Crear gasto funciona igual (con/sin recibo).
2. Editar y borrar gasto funcionan.
3. Categorías CRUD funciona.
4. Reportes por mes/moneda muestran mismos resultados.
5. Exportación CSV genera columnas esperadas.
6. Flujo PIN (set, lock, verify, disable) se mantiene.

Si un criterio falla, rollback del slice únicamente (no del proyecto completo).

---

## 7) Recomendación práctica inmediata (siguiente paso)

Empezar por **Fase 1 (Expenses vertical slice)** porque:
- impacta el corazón del producto,
- reduce deuda en validación/errores,
- es el mejor punto para introducir patrón de casos de uso que luego replicarás en reports/categories/security.

Si quieres, en el siguiente paso te puedo preparar un **esqueleto exacto de archivos Kotlin** para Fase 1 (interfaces, modelos, use cases y adaptación del ViewModel) para implementarlo en pequeñas PRs.
