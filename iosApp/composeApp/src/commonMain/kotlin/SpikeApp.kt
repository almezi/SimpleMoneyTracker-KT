// STAGE 1 SPIKE — throwaway.
//
// Uses Saku's REAL tag strings from TestTags.kt, not invented ones, so this
// spike answers "will the existing automation suite's locators work on iOS?"
// rather than a weaker proxy question.
//
// The five patterns under test:
//   1. root container  -> tambah_screen          (CMP-9551 risk)
//   2. text input     -> tambah_input_amount
//   3. primary button -> tambah_button_save
//   4. list item      -> daftar_item
//   5. sheet confirm  -> component_sheet_button_confirm

package spike

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpikeScreen() {
    var amount by remember { mutableStateOf("") }
    var sheetOpen by remember { mutableStateOf(false) }

    // NOTE: opening a ModalBottomSheet empties the iOS accessibility tree when
    // read from inside the app with no accessibility consumer attached — see
    // docs/09 §4.2. The spike therefore leaves the sheet closed by default so
    // the verified configuration stays reproducible. Flip AUTO_OPEN_SHEET to
    // true to reproduce the empty-tree behaviour.
    val autoOpenSheet = false
    LaunchedEffect(Unit) {
        if (!autoOpenSheet) return@LaunchedEffect
        kotlinx.coroutines.delay(3000)
        sheetOpen = true
    }

    // PATTERN 1 — root container tag. This is the CMP-9551 risk: a tag on a
    // container may surface on a synthetic accessibility child instead of the
    // container element itself. Saku tags the root Box/Column of every screen.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0E0E12))
            .testTag("tambah_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Stage 1 Spike", color = Color(0xFFF2F2F5))

            // PATTERN 2 — interactive text input
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("JUMLAH") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tambah_input_amount")
            )

            // PATTERN 3 — primary button
            Button(
                onClick = { sheetOpen = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tambah_button_save")
            ) { Text("Simpan transaksi") }

            // PATTERN 4 — list item inside a LazyColumn
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("daftar_list")
            ) {
                items(listOf("Gaji", "Makanan & Minuman", "Bahan Bakar")) { label ->
                    Text(
                        text = label,
                        color = Color(0xFFF2F2F5),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .testTag("daftar_item")
                    )
                }
            }
        }
    }

    // PATTERN 5 — confirm button inside a bottom sheet
    if (sheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { sheetOpen = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = Color(0xFF17171D)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("Hapus transaksi?", color = Color(0xFFF2F2F5))
                Button(
                    onClick = { sheetOpen = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .testTag("component_sheet_button_confirm")
                ) { Text("Ya, hapus") }
            }
        }
    }
}