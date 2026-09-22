package id.almezi.simplemoneytracker_kt.ui.tambah

import android.app.DatePickerDialog
import android.icu.util.Calendar
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.almezi.simplemoneytracker_kt.TestTags

@Composable
fun TambahScreen() {
    var type by remember { mutableStateOf("pemasukan") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = if (type == "pemasukan") "Tambah Pemasukan" else "Tambak Pengeluaran",
            style = MaterialTheme.typography.headlineSmall
        )

        // Type toggle
        Row(modifier = Modifier.padding(vertical = 8.dp)) {
            FilterChip(
                selected = type == "pemasukan",
                onClick = { type = "pemasukan" },
                label = { Text("Pemasukan") },
                modifier = Modifier.testTag(TestTags.TAMBAH_TOGGLE_TYPE + "_pemasukan")
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilterChip(
                selected = type == "pengeluaran",
                onClick = { type = "pengeluaran" },
                label = { Text("Pengeluaran") },
                modifier = Modifier.testTag(TestTags.TAMBAH_TOGGLE_TYPE + "_pengeluaran")
            )
        }

        // Amount
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("Jumlah") },
            modifier = Modifier.fillMaxWidth().testTag(TestTags.TAMBAH_INPUT_AMOUNT)
        )

        // Category (grouped expense — placeholder for CATEGORIES.md groups)
        Text(
            text = "Kategori",
            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
        )
        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            listOf("Makanan", "Transportasi", "Tagihan", "Hiburan").forEach { cat ->
                FilterChip(
                    selected = false,
                    onClick = { /* TODO: bind CATEGORIES.md IDs */ },
                    label = { Text(cat) },
                    modifier = Modifier.padding(end = 4.dp).testTag(TestTags.TAMBAH_CATEGORY_ITEM + "_" + cat)
                )
            }
        }

        // Date picker
        OutlinedButton(
            onClick = {
                DatePickerDialog(context, { _, y, m, d ->
                    calendar.set(y, m, d)
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
            },
            modifier = Modifier.padding(vertical = 8.dp).testTag(TestTags.TAMBAH_DATE_PICKER)
        ) {
            Text(text = "Pilih Tanggal")
        }

        // Note
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Catatan") },
            modifier = Modifier.fillMaxWidth().testTag(TestTags.TAMBAH_INPUT_NOTE)
        )

        // Save
        Button(
            onClick = { /* TODO: save to DB — MVVM slice next */ },
            modifier = Modifier.padding(top = 16.dp).fillMaxWidth().testTag(TestTags.TAMBAH_BUTTON_SAVE)
        ) {
            Text("Simpan")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TambahScreenPreview() {
    MaterialTheme {
        TambahScreen()
    }
}
