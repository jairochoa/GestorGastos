package com.example.gestorgastos.ui.nav

import android.content.ContentResolver
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gestorgastos.GastosApp
import com.example.gestorgastos.data.local.model.CurrencyCode
import com.example.gestorgastos.ui.AddExpenseVMFactory
import com.example.gestorgastos.ui.CategoriesVMFactory
import com.example.gestorgastos.ui.EditExpenseVMFactory
import com.example.gestorgastos.ui.ExpensesListVMFactory
import com.example.gestorgastos.ui.add.AddExpenseScreen
import com.example.gestorgastos.ui.add.AddExpenseViewModel
import com.example.gestorgastos.ui.amountMinorToDecimalString
import com.example.gestorgastos.ui.categories.CategoriesScreen
import com.example.gestorgastos.ui.categories.CategoriesViewModel
import com.example.gestorgastos.ui.edit.EditExpenseScreen
import com.example.gestorgastos.ui.edit.EditExpenseViewModel
import com.example.gestorgastos.ui.formatEpochDay
import com.example.gestorgastos.ui.list.ExpensesListScreen
import com.example.gestorgastos.ui.list.ExpensesListViewModel
import com.example.gestorgastos.ui.lock.LockScreen
import com.example.gestorgastos.ui.lock.ManagePinScreen
import com.example.gestorgastos.ui.lock.SetupPinScreen
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets

private object Routes {
    const val LOCK = "lock"
    const val SETUP_PIN = "setup_pin"
    const val LIST = "list"
    const val ADD = "add"
    const val MANAGE_PIN = "manage_pin"
    const val CATEGORIES = "categories"
    const val EDIT = "edit"
    const val REPORTS = "reports"
}

@Composable
fun AppNav() {
    val context = LocalContext.current
    val app = context.applicationContext as GastosApp
    val container = app.container
    val security = container.securityManager

    var pinSet by remember { mutableStateOf(security.isPinSet()) }

    val navController = rememberNavController()
    val startDest = remember(pinSet) { if (pinSet) Routes.LOCK else Routes.LIST }

    NavHost(navController = navController, startDestination = startDest) {

        // ---------------- EDIT ----------------
        composable("${Routes.EDIT}/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toLongOrNull()
            if (id == null) {
                navController.popBackStack()
                return@composable
            }

            val editVm: EditExpenseViewModel =
                viewModel(factory = EditExpenseVMFactory(container.expenseRepository))

            val catVm: CategoriesViewModel =
                viewModel(factory = CategoriesVMFactory(container.categoryRepository))
            val categories by catVm.categories.collectAsStateWithLifecycle()

            val editState by editVm.state.collectAsStateWithLifecycle()
            LaunchedEffect(id) { editVm.load(id) }

            val expense = editState.expense
            if (editState.loaded && expense == null) {
                navController.popBackStack()
                return@composable
            }

            if (expense != null) {
                EditExpenseScreen(
                    expense = expense,
                    categories = categories,
                    onBack = { navController.popBackStack() },
                    onSave = { amount, currency, concept, merchant, address, desc, method, receiptUri, categoryId ->
                        val ok = editVm.save(
                            original = expense,
                            amountText = amount,
                            currency = currency,
                            concept = concept,
                            merchant = merchant,
                            address = address,
                            description = desc,
                            paymentMethod = method,
                            receiptUri = receiptUri,
                            categoryId = categoryId
                        )
                        if (ok) navController.popBackStack()
                    },
                    onDelete = {
                        editVm.delete(expense)
                        navController.popBackStack()
                    }
                )
            }
        }
        // ---------------- REPORTES ----------------
        composable(Routes.REPORTS) {
            val vm: com.example.gestorgastos.ui.reports.ReportsViewModel =
                viewModel(factory = com.example.gestorgastos.ui.ReportsVMFactory(container.expenseRepository))

            val state by vm.state.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) { vm.load() }

            com.example.gestorgastos.ui.reports.ReportsScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onPrevMonth = vm::prevMonth,
                onNextMonth = vm::nextMonth,
                onCurrency = vm::setCurrency
            )
        }

        // ---------------- MANAGE PIN ----------------
        composable(Routes.MANAGE_PIN) {
            ManagePinScreen(
                security = security,
                onBack = { navController.popBackStack() },
                onVerifiedToChange = { navController.navigate(Routes.SETUP_PIN) },
                onPinDisabled = {
                    pinSet = false
                    navController.navigate(Routes.LIST) {
                        popUpTo(Routes.MANAGE_PIN) { inclusive = true }
                    }
                }
            )
        }

        // ---------------- LOCK ----------------
        composable(Routes.LOCK) {
            LockScreen(
                security = security,
                onUnlocked = {
                    navController.navigate(Routes.LIST) {
                        popUpTo(Routes.LOCK) { inclusive = true }
                    }
                },
                onSetupPin = { navController.navigate(Routes.SETUP_PIN) }
            )
        }

        // ---------------- SETUP PIN ----------------
        composable(Routes.SETUP_PIN) {
            SetupPinScreen(
                security = security,
                onPinSet = {
                    pinSet = true
                    navController.navigate(Routes.LIST) {
                        popUpTo(Routes.SETUP_PIN) { inclusive = true }
                        popUpTo(Routes.LOCK) { inclusive = true }
                    }
                }
            )
        }

        // ---------------- CATEGORIES ----------------
        composable(Routes.CATEGORIES) {
            val vm: CategoriesViewModel =
                viewModel(factory = CategoriesVMFactory(container.categoryRepository))
            val categories by vm.categories.collectAsStateWithLifecycle()

            CategoriesScreen(
                categories = categories,
                onBack = { navController.popBackStack() },
                onAdd = vm::add,
                onRename = vm::rename,
                onDelete = vm::delete
            )
        }

        // ---------------- LIST ----------------
        composable(Routes.LIST) {
            val vm: ExpensesListViewModel =
                viewModel(factory = ExpensesListVMFactory(container.expenseRepository))
            val state by vm.uiState.collectAsStateWithLifecycle()

            val scope = rememberCoroutineScope()

            var pendingCsv by remember { mutableStateOf<String?>(null) }
            val exportLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.CreateDocument("text/csv")
            ) { uri: Uri? ->
                val csv = pendingCsv ?: return@rememberLauncherForActivityResult
                if (uri != null) writeText(context.contentResolver, uri, csv)
                pendingCsv = null
            }

            ExpensesListScreen(
                state = state,
                onPrevMonth = vm::prevMonth,
                onNextMonth = vm::nextMonth,
                onInsertDemo = vm::insertDemo,
                onCategories = { navController.navigate(Routes.CATEGORIES) },
                onAdd = { navController.navigate(Routes.ADD) },
                onReports = { navController.navigate(Routes.REPORTS) },
                onExportCsv = {
                    scope.launch {
                        val csv = buildCsvFromRows(state.rows)
                        pendingCsv = csv
                        exportLauncher.launch("gastos_${state.month}.csv")
                    }
                },
                onOpenExpense = { id ->
                    navController.navigate("${Routes.EDIT}/$id")
                },
                onSetupPin = {
                    if (security.isPinSet()) navController.navigate(Routes.MANAGE_PIN)
                    else navController.navigate(Routes.SETUP_PIN)
                }
            )
        }

        // ---------------- ADD ----------------
        composable(Routes.ADD) {
            val vm: AddExpenseViewModel =
                viewModel(factory = AddExpenseVMFactory(container.expenseRepository))

            val catVm: CategoriesViewModel =
                viewModel(factory = CategoriesVMFactory(container.categoryRepository))
            val categories by catVm.categories.collectAsStateWithLifecycle()

            AddExpenseScreen(
                onBack = { navController.popBackStack() },
                categories = categories,
                onManageCategories = { navController.navigate(Routes.CATEGORIES) },
                onSave = { amount, currency, concept, merchant, address, desc, method, receiptUri, categoryId ->
                    val ok = vm.save(
                        amountText = amount,
                        currency = currency,
                        concept = concept,
                        merchant = merchant,
                        address = address,
                        description = desc,
                        paymentMethod = method,
                        receiptUri = receiptUri,
                        categoryId = categoryId
                    )
                    if (ok) navController.popBackStack()
                }
            )
        }
    }
}

private fun writeText(contentResolver: ContentResolver, uri: Uri, text: String) {
    contentResolver.openOutputStream(uri)?.use { os ->
        os.write(text.toByteArray(StandardCharsets.UTF_8))
        os.flush()
    }
}

private const val CSV_SEP = ';'

private fun buildCsvFromRows(rows: List<com.example.gestorgastos.data.local.dao.ExpenseListRow>): String {
    val sb = StringBuilder()

    sb.append(
        listOf(
            "date",
            "currency",
            "amount",
            "amountMinor",
            "concept",
            "category",
            "merchant",
            "address",
            "paymentMethod",
            "description",
            "receiptUri"
        ).joinToString(CSV_SEP.toString())
    ).append('\n')

    for (r in rows) {
        val e = r.expense
        val currency = runCatching { CurrencyCode.valueOf(e.currency) }.getOrNull()
        val amount = currency?.let { amountMinorToDecimalString(e.amountMinor, it.decimals) }
            ?: e.amountMinor.toString()

        sb.append(csvSafe(formatEpochDay(e.dateEpochDay))).append(CSV_SEP)
            .append(csvSafe(e.currency)).append(CSV_SEP)
            .append(csvSafe(amount)).append(CSV_SEP)
            .append(e.amountMinor).append(CSV_SEP)
            .append(csvSafe(e.concept)).append(CSV_SEP)
            .append(csvSafe(r.categoryName ?: "")).append(CSV_SEP)
            .append(csvSafe(e.merchant ?: "")).append(CSV_SEP)
            .append(csvSafe(e.address ?: "")).append(CSV_SEP)
            .append(csvSafe(e.paymentMethod)).append(CSV_SEP)
            .append(csvSafe(e.description ?: "")).append(CSV_SEP)
            .append(csvSafe(e.receiptUri ?: ""))
            .append('\n')
    }
    return sb.toString()
}

private fun csvSafe(s: String): String {
    val needsQuotes = s.contains(CSV_SEP) || s.contains('"') || s.contains('\n') || s.contains('\r')
    val escaped = s.replace("\"", "\"\"")
    return if (needsQuotes) "\"$escaped\"" else escaped
}