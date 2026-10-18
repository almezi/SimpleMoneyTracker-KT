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
import id.almezi.simplemoneytracker_kt.data.AccountSeed
import id.almezi.simplemoneytracker_kt.ui.components.PrimaryButtonTone
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuTheme
import id.almezi.simplemoneytracker_kt.ui.designsystem.TransactionType
import id.almezi.simplemoneytracker_kt.ui.tambah.TambahUiState
import id.almezi.simplemoneytracker_kt.ui.tambah.switchedTo
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
 * This drives `TransactionForm` directly, switching the state through
 * `TambahUiState.switchedTo` — the same transition `TambahViewModel.updateType`
 * calls — so the test covers the real rule and only isolates the UI layer where
 * the reset was observed.
 */
class TransactionFormTypeSwitchTest {

    @Test
    fun switchingTypeKeepsTypedAmount() = runComposeUiTest {
        // Switches a real TambahUiState through the production `switchedTo`, so
        // this test fails if the rule changes. It used to reimplement the rule
        // locally, which is how it passed while TambahViewModel wiped the amount.
        var form by mutableStateOf(
            TambahUiState(dateMillis = 0L, selectedAccountId = AccountSeed.DEFAULT_ACCOUNT_ID)
        )

        setContent {
            SakuTheme {
                TransactionForm(
                    state = TransactionFormState(
                        type = form.type,
                        amountDigits = form.amountDigits,
                        categories = emptyList(),
                        selectedCategoryId = form.selectedCategoryId,
                        accounts = emptyList(),
                        selectedAccountId = form.selectedAccountId,
                        dateMillis = form.dateMillis,
                        note = form.note,
                    ),
                    actions = TransactionFormActions(
                        onTypeChange = { newType -> form = form.switchedTo(newType, emptyList()) },
                        onAmountChange = { digits -> form = form.copy(amountDigits = digits) },
                        onCategoryChange = { id -> form = form.copy(selectedCategoryId = id) },
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

        assertEquals("5000", form.amountDigits, "typing should record the digits")
        onNodeWithText("5.000", substring = true)
            .assertExists("amount should render as Rp 5.000 after typing")

        onNodeWithTag(TestTags.COMPONENT_TOGGLE_SEGMENTED + "_1").performClick()

        assertEquals(TransactionType.Expense, form.type, "toggle should switch the type")
        assertEquals(
            "5000",
            form.amountDigits,
            "switching type must keep the amount (UI_SPEC.md:14)",
        )
        onNodeWithText("5.000", substring = true)
            .assertExists("amount field must still show Rp 5.000 after switching type")
    }
}
