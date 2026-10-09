package id.almezi.simplemoneytracker_kt.data

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

const val BACKUP_VERSION = 1

data class BackupData(
    val transactions: List<Transaction>,
    val categories: List<BackupCategory>,
    val accounts: List<Account>,
    val budgets: List<Budget>
)

data class BackupCategory(
    val id: String,
    val name: String,
    val type: String,
    val colorHex: String,
    val isHidden: Boolean
)

class BackupRepository(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository,
    private val accountDao: AccountDao
) {

    suspend fun buildBackup(): BackupData = BackupData(
        transactions = transactionRepository.getAllTransactionsOnce(),
        categories = categoryRepository.getByTypeOnce("pemasukan")
            .plus(categoryRepository.getByTypeOnce("pengeluaran"))
            .filter { !it.isDefault }
            .map { BackupCategory(it.id, it.customName.orEmpty(), it.type, it.colorHex.orEmpty(), it.isHidden) },
        accounts = accountDao.getAllOnce(),
        budgets = budgetRepository.getAllBudgetsOnce()
    )

    fun toJson(backup: BackupData): String {
        val root = JSONObject()
        root.put("version", BACKUP_VERSION)
        val transactions = JSONArray()
        backup.transactions.forEach { tx ->
            transactions.put(
                JSONObject().apply {
                    put("type", tx.type)
                    put("amount", tx.amount)
                    put("categoryId", tx.categoryId)
                    put("accountId", tx.accountId)
                    put("dateEpochMillis", tx.dateEpochMillis)
                    put("note", tx.note ?: JSONObject.NULL)
                }
            )
        }
        root.put("transactions", transactions)
        val categories = JSONArray()
        backup.categories.forEach { category ->
            categories.put(
                JSONObject().apply {
                    put("id", category.id)
                    put("name", category.name)
                    put("type", category.type)
                    put("color", category.colorHex)
                    put("hidden", category.isHidden)
                }
            )
        }
        root.put("categories", categories)
        val budgets = JSONArray()
        backup.budgets.forEach { budget ->
            budgets.put(
                JSONObject().apply {
                    put("categoryId", budget.categoryId)
                    put("limit", budget.limitAmount)
                    put("alertAt80", budget.alertAt80)
                }
            )
        }
        root.put("budgets", budgets)
        val accounts = JSONArray()
        backup.accounts.forEach { account ->
            accounts.put(
                JSONObject().apply {
                    put("id", account.id)
                    put("name", account.name)
                    put("sortOrder", account.sortOrder)
                }
            )
        }
        root.put("accounts", accounts)
        return root.toString(2)
    }

    fun toCsv(backup: BackupData): String {
        val builder = StringBuilder()
        builder.append('﻿')
        builder.append("date,type,amount,category,account,note\n")
        backup.transactions.sortedBy { it.dateEpochMillis }.forEach { tx ->
            builder.append(dateString(tx.dateEpochMillis)).append(',')
                .append(if (tx.type == "pemasukan") "income" else "expense").append(',')
                .append(tx.amount).append(',')
                .append(csvEscape(tx.categoryId)).append(',')
                .append(csvEscape(tx.accountId)).append(',')
                .append(csvEscape(tx.note.orEmpty()))
                .append('\n')
        }
        return builder.toString()
    }

    fun writeFile(context: Context, fileName: String, content: String, mimeType: String): java.io.File {
        val file = context.getExternalFilesDir(null)?.resolve(fileName)
            ?: context.filesDir.resolve(fileName)
        file.writeText(content, StandardCharsets.UTF_8)
        return file
    }

    fun readText(context: Context, uri: Uri): String =
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).readText()
        } ?: ""

    fun parseJson(raw: String): BackupData {
        val root = JSONObject(raw)
        val transactions = ArrayList<Transaction>()
        val array = root.optJSONArray("transactions") ?: JSONArray()
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            transactions.add(
                Transaction(
                    id = 0,
                    type = item.optString("type", "pengeluaran"),
                    amount = item.optLong("amount", 0L),
                    categoryId = item.optString("categoryId"),
                    accountId = item.optString("accountId", AccountSeed.DEFAULT_ACCOUNT_ID),
                    dateEpochMillis = item.optLong("dateEpochMillis", 0L),
                    note = if (item.isNull("note")) null else item.optString("note"),
                    createdAt = item.optLong("createdAt", 0L)
                )
            )
        }
        val categories = ArrayList<BackupCategory>()
        val categoryArray = root.optJSONArray("categories") ?: JSONArray()
        for (i in 0 until categoryArray.length()) {
            val item = categoryArray.getJSONObject(i)
            categories.add(
                BackupCategory(
                    id = item.optString("id"),
                    name = item.optString("name"),
                    type = item.optString("type", "pengeluaran"),
                    colorHex = item.optString("color"),
                    isHidden = item.optBoolean("hidden", false)
                )
            )
        }
        val budgets = ArrayList<Budget>()
        val budgetArray = root.optJSONArray("budgets") ?: JSONArray()
        for (i in 0 until budgetArray.length()) {
            val item = budgetArray.getJSONObject(i)
            budgets.add(
                Budget(
                    categoryId = item.optString("categoryId"),
                    limitAmount = item.optLong("limit", 0L),
                    alertAt80 = item.optBoolean("alertAt80", true),
                    createdAt = item.optLong("createdAt", 0L),
                    updatedAt = item.optLong("updatedAt", 0L)
                )
            )
        }
        return BackupData(transactions, categories, emptyList(), budgets)
    }

    fun dateString(epochMillis: Long): String {
        val calendar = Calendar.getInstance(JAKARTA).apply { timeInMillis = epochMillis }
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)
    }

    private val JAKARTA: TimeZone = TimeZone.getTimeZone("Asia/Jakarta")

    private fun csvEscape(value: String): String =
        if (value.contains(',') || value.contains('"')) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
}