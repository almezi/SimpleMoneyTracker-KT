// The Compose Multiplatform UI test entry points (`runComposeUiTest` and the
// semantics assertions) are marked experimental in 1.12.1.
@file:OptIn(androidx.compose.ui.test.ExperimentalTestApi::class)

package id.almezi.simplemoneytracker_kt.ui.transaction

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import id.almezi.simplemoneytracker_kt.SakuStrings
import id.almezi.simplemoneytracker_kt.TestTags
import id.almezi.simplemoneytracker_kt.ui.components.PrimaryButtonTone
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuTheme
import id.almezi.simplemoneytracker_kt.ui.designsystem.TransactionType
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Regression test for the reported Tambah bug: switching the type toggle from
 * Pemasukan to Pengeluaran must keep the amount typed in Jumlah.
 *
 * `UI_SPEC.md` line 14 and `improve/02-screens-and-rules.md` line 47 both say
 * switching type keeps the amount and the note, and only clears a category that
 * does not match the new type.
 *
 * This drives `TransactionForm` directly with a state holder that mirrors
 * `TambahViewModel.updateType` (amount preserved, category cleared on mismatch),
 * so the test isolates the UI layer where the reset was observed.
 */
class TransactionFormTypeSwitchTest {

    @Test
    fun switchingTypeKeepsTypedAmount() = runComposeUiTest {
        var type by mutableStateOf(TransactionType.Income)
        var amountDigits by mutableStateOf("")
        var selectedCategoryId by mutableStateOf<String?>(null)

        setContent {
            SakuTheme {
                TransactionForm(
                    state = TransactionFormState(
                        type = type,
                        amountDigits = amountDigits,
                        categories = emptyList(),
                        selectedCategoryId = selectedCategoryId,
                        accounts = emptyList(),
                        selectedAccountId = "cash",
                        dateMillis = 0L,
                        note = "",
                    ),
                    actions = TransactionFormActions(
                        // Mirrors TambahViewModel.updateType: amount and note
                        // are untouched, the category is dropped when it does
                        // not match the new type.
                        onTypeChange = { newType ->
                            type = newType
                            selectedCategoryId = null
                        },
                        onAmountChange = { amountDigits = it },
                        onCategoryChange = { selectedCategoryId = it },
                        onAccountChange = {},
                        onDateChange = {},
                        onNoteChange = {},
                    ),
                    header = null,
                    saveLabel = SakuStrings.tambah_simpan_transaksi,
                    saveEnabled = false,
                    helperText = null,
                    saveTone = PrimaryButtonTone.Brand,
                    showKelolaKategoriLink = true,
                    showQuickCategoryChips = false,
                    onOpenKelolaKategori = {},
                    onSave = {},
                    maxDateMillis = 0L,
                    bottomReserve = 0.dp,
                    testTag = TestTags.TAMBAH_BUTTON_SAVE,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        // The amount field's test tag sits on the wrapping Surface, which has no
        // text semantics, so target the editable node itself. It is the first
        // editable field in the form (note comes after it).
        val amountField = onAllNodes(hasSetTextAction())[0]

        amountField.performTextInput("5000")

        assertEquals("5000", amountDigits, "typing should record the digits")
        onNodeWithText("5.000", substring = true)
            .assertExists("amount should render as Rp 5.000 after typing")

        onNodeWithTag(TestTags.COMPONENT_TOGGLE_SEGMENTED + "_1").performClick()

        assertEquals(TransactionType.Expense, type, "toggle should switch the type")
        assertEquals(
            "5000",
            amountDigits,
            "switching type must keep the amount (UI_SPEC.md:14)",
        )
        onNodeWithText("5.000", substring = true)
            .assertExists("amount field must still show Rp 5.000 after switching type")
    }
}
