package id.almezi.simplemoneytracker_kt.ui.tambah

import android.app.DatePickerDialog
import android.icu.util.Calendar
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.almezi.simplemoneytracker_kt.R
import id.almezi.simplemoneytracker_kt.SimpleMoneyTrackerApp
import id.almezi.simplemoneytracker_kt.TestTags
import id.almezi.simplemoneytracker_kt.ui.getCategoryNameRes
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun TambahScreen(
    modifier: Modifier = Modifier,
    viewModel: TambahViewModel = viewModel(
        factory = TambahViewModel.Factory(
            (LocalContext.current.applicationContext as SimpleMoneyTrackerApp).container.transactionRepository,
            (LocalContext.current.applicationContext as SimpleMoneyTrackerApp).container.clock
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")) }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text(
                text = stringResource(
                    id = if (uiState.type == "pemasukan") R.string.tambah_screen_title_pemasukan
                    else R.string.tambah_screen_title_pengeluaran
                ),
                style = MaterialTheme.typography.headlineSmall
            )

            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                FilterChip(
                    selected = uiState.type == "pemasukan",
                    onClick = { viewModel.updateType("pemasukan") },
                    label = { Text(stringResource(R.string.tambah_type_pemasukan)) },
                    modifier = Modifier.testTag(TestTags.TAMBAH_TOGGLE_TYPE + "_pemasukan")
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = uiState.type == "pengeluaran",
                    onClick = { viewModel.updateType("pengeluaran") },
                    label = { Text(stringResource(R.string.tambah_type_pengeluaran)) },
                    modifier = Modifier.testTag(TestTags.TAMBAH_TOGGLE_TYPE + "_pengeluaran")
                )
            }

            OutlinedTextField(
                value = uiState.amount,
                onValueChange = { viewModel.updateAmount(it.filter { c -> c.isDigit() }) },
                label = { Text(stringResource(R.string.tambah_amount_label)) },
                modifier = Modifier.fillMaxWidth().testTag(TestTags.TAMBAH_INPUT_AMOUNT),
                isError = uiState.amountError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            if (uiState.amountError) {
                Text(
                    text = stringResource(R.string.tambah_error_amount),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.testTag(TestTags.TAMBAH_ERROR_AMOUNT)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.tambah_category_label),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                categories.forEach { category ->
                    FilterChip(
                        selected = uiState.selectedCategoryId == category.id,
                        onClick = { viewModel.updateCategory(category.id) },
                        label = { Text(stringResource(getCategoryNameRes(category.id))) },
                        modifier = Modifier.padding(end = 4.dp)
                            .testTag(TestTags.TAMBAH_CATEGORY_ITEM + "_" + category.id)
                    )
                }
            }
            if (uiState.categoryError) {
                Text(
                    text = stringResource(R.string.tambah_error_category),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.testTag(TestTags.TAMBAH_ERROR_CATEGORY)
                )
            }

            OutlinedButton(
                onClick = {
                    DatePickerDialog(
                        context,
                        { _, y, m, d ->
                            val cal = Calendar.getInstance()
                            cal.set(y, m, d, 0, 0, 0)
                            cal.set(Calendar.MILLISECOND, 0)
                            viewModel.updateDate(cal.timeInMillis)
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                },
                modifier = Modifier.padding(vertical = 8.dp)
                    .testTag(TestTags.TAMBAH_DATE_PICKER)
            ) {
                Text(text = dateFormat.format(uiState.dateMillis))
            }

            OutlinedTextField(
                value = uiState.note,
                onValueChange = { viewModel.updateNote(it) },
                label = { Text(stringResource(R.string.tambah_note_label)) },
                modifier = Modifier.fillMaxWidth().testTag(TestTags.TAMBAH_INPUT_NOTE)
            )

            Button(
                onClick = { viewModel.save() },
                modifier = Modifier.padding(top = 16.dp)
                    .fillMaxWidth()
                    .testTag(TestTags.TAMBAH_BUTTON_SAVE)
            ) {
                Text(stringResource(R.string.tambah_save_button))
            }
        }
    }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            snackbarHostState.showSnackbar(context.getString(R.string.tambah_save_success))
            viewModel.resetSaveState()
        }
    }
}
