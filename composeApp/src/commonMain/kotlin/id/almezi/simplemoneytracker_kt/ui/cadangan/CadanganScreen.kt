package id.almezi.simplemoneytracker_kt.ui.cadangan

import id.almezi.simplemoneytracker_kt.SakuContainer
import id.almezi.simplemoneytracker_kt.SakuStrings
import id.almezi.simplemoneytracker_kt.format
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import id.almezi.simplemoneytracker_kt.TestTags
import id.almezi.simplemoneytracker_kt.data.backup.rememberBackupFileStore
import id.almezi.simplemoneytracker_kt.data.backup.rememberFilePicker
import id.almezi.simplemoneytracker_kt.ui.components.HelperText
import id.almezi.simplemoneytracker_kt.ui.components.PrimaryButton
import id.almezi.simplemoneytracker_kt.ui.components.SakuCard
import id.almezi.simplemoneytracker_kt.ui.components.SakuToast
import id.almezi.simplemoneytracker_kt.ui.components.SakuToastHost
import id.almezi.simplemoneytracker_kt.ui.components.ScreenHeader
import id.almezi.simplemoneytracker_kt.ui.components.SecondaryButton
import id.almezi.simplemoneytracker_kt.ui.components.SectionLabel
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuRadius
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuSpace
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuTheme
import id.almezi.simplemoneytracker_kt.ui.designsystem.formatShortDate
import id.almezi.simplemoneytracker_kt.ui.designsystem.safeAreaBottom
import id.almezi.simplemoneytracker_kt.ui.designsystem.safeAreaTop
import kotlinx.coroutines.launch

@Composable
fun CadanganScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // SakuContainer is a @Composable getter, so it must be read here, in the
    // composable body. An `initializer { }` block is not composable and cannot
    // call it.
    val container = SakuContainer
    val viewModel: CadanganViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                CadanganViewModel(
                    container.backupSerializer,
                    container.backupPreferences,
                    container.transactionRepository,
                    container.clock,
                )
            }
        },
    )
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val filePicker = rememberFilePicker()
    val backupFileStore = rememberBackupFileStore()
    val toastText = uiState.toast?.let { path ->
        SakuStrings.cadangan_export_saved.format(path)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SakuTheme.colors.bg)
            .testTag(TestTags.CADANGAN_SCREEN),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeAreaTop()
                .safeAreaBottom()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SakuSpace.screenHorizontal)
                .padding(top = SakuSpace.screenHorizontal, bottom = SakuSpace.screenBottom),
            verticalArrangement = Arrangement.spacedBy(SakuSpace.screenHorizontal),
        ) {
            ScreenHeader(title = SakuStrings.cadangan_title, onBack = onBack)

            SakuCard(
                modifier = Modifier.fillMaxWidth().testTag(TestTags.CADANGAN_STATUS_CARD)
            ) {
                Column {
                    Text(
                        text = SakuStrings.cadangan_status_title,
                        style = SakuTheme.text.bodyStrong,
                        color = SakuTheme.colors.text,
                    )
                    Spacer(modifier = Modifier.height(SakuSpace.tight))
                    Text(
                        text = SakuStrings.cadangan_transaction_count.format(uiState.transactionCount),
                        style = SakuTheme.text.caption,
                        color = SakuTheme.colors.textMuted,
                    )
                    Text(
                        text = uiState.lastBackupMillis?.let {
                            SakuStrings.cadangan_last_backup.format(formatShortDate(it))
                        } ?: SakuStrings.cadangan_never,
                        style = SakuTheme.text.caption,
                        color = SakuTheme.colors.textMuted,
                    )
                }
            }

            Column {
                SectionLabel(text = SakuStrings.cadangan_export)
                Spacer(modifier = Modifier.height(SakuSpace.cardInner))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SakuSpace.chipGap),
                ) {
                    SecondaryButton(
                        text = SakuStrings.cadangan_export_csv,
                        onClick = {
                            scope.launch {
                                val path = backupFileStore.shareText(
                                    "saku-cadangan.csv",
                                    viewModel.buildCsv(),
                                    "text/csv",
                                )
                                viewModel.markBackupDone(path)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        testTag = TestTags.CADANGAN_EXPORT_CSV,
                    )
                    SecondaryButton(
                        text = SakuStrings.cadangan_export_json,
                        onClick = {
                            scope.launch {
                                val path = backupFileStore.shareText(
                                    "saku-cadangan.json",
                                    viewModel.buildJson(),
                                    "application/json",
                                )
                                viewModel.markBackupDone(path)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        testTag = TestTags.CADANGAN_EXPORT_JSON,
                    )
                }
            }

            Column {
                SectionLabel(text = SakuStrings.cadangan_import)
                Spacer(modifier = Modifier.height(SakuSpace.cardInner))
                PrimaryButton(
                    text = if (uiState.importing) {
                        SakuStrings.cadangan_importing
                    } else {
                        SakuStrings.cadangan_import_button
                    },
                    onClick = {
                        filePicker.present { raw ->
                            if (raw != null) viewModel.import(raw)
                        }
                    },
                    enabled = !uiState.importing,
                    testTag = TestTags.CADANGAN_IMPORT_BUTTON,
                )
                Spacer(modifier = Modifier.height(SakuSpace.chipGap))
                HelperText(text = SakuStrings.cadangan_import_note)
                if (uiState.importResult != null) {
                    Spacer(modifier = Modifier.height(SakuSpace.chipGap))
                    HelperText(
                        text = SakuStrings.cadangan_import_result.format(
                            uiState.importResult!!.added,
                            uiState.importResult!!.skipped
                        ),
                        color = SakuTheme.colors.income,
                        modifier = Modifier.testTag(TestTags.CADANGAN_IMPORT_RESULT),
                    )
                }
                if (uiState.importError) {
                    Spacer(modifier = Modifier.height(SakuSpace.chipGap))
                    HelperText(
                        text = SakuStrings.cadangan_import_error,
                        color = SakuTheme.colors.danger,
                        modifier = Modifier.testTag(TestTags.CADANGAN_IMPORT_ERROR),
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth().testTag(TestTags.CADANGAN_WARNING),
                shape = RoundedCornerShape(SakuRadius.card),
                color = SakuTheme.colors.expenseSurface,
                contentColor = SakuTheme.colors.text,
            ) {
                Text(
                    text = SakuStrings.cadangan_warning,
                    style = SakuTheme.text.caption,
                    color = SakuTheme.colors.textSecondary,
                    modifier = Modifier.padding(SakuSpace.cardInner + SakuSpace.tight),
                )
            }
        }

        SakuToastHost(
            toast = toastText?.let { SakuToast(text = it) },
            onDismiss = viewModel::clearToast,
        )
    }
}