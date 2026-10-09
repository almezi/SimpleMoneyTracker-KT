package id.almezi.simplemoneytracker_kt.ui.cadangan

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import id.almezi.simplemoneytracker_kt.R
import id.almezi.simplemoneytracker_kt.SimpleMoneyTrackerApp
import id.almezi.simplemoneytracker_kt.TestTags
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
import kotlinx.coroutines.launch

@Composable
fun CadanganScreen(
    onBack: () -> Unit,
    onAddTransaction: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CadanganViewModel = viewModel(
        factory = CadanganViewModel.Factory(
            LocalContext.current.applicationContext,
            (LocalContext.current.applicationContext as SimpleMoneyTrackerApp).container.backupRepository,
            (LocalContext.current.applicationContext as SimpleMoneyTrackerApp).container.transactionRepository,
            (LocalContext.current.applicationContext as SimpleMoneyTrackerApp).container.categoryRepository,
            (LocalContext.current.applicationContext as SimpleMoneyTrackerApp).container.clock
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val container = (LocalContext.current.applicationContext as SimpleMoneyTrackerApp).container

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val raw = container.backupRepository.readText(context, uri)
            viewModel.import(raw)
        }
    }

    val toastText = uiState.toast?.let { path ->
        context.getString(R.string.cadangan_export_saved, path)
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
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = SakuSpace.screenHorizontal)
                .padding(top = SakuSpace.screenHorizontal, bottom = SakuSpace.screenBottom),
            verticalArrangement = Arrangement.spacedBy(SakuSpace.screenHorizontal),
        ) {
            ScreenHeader(title = stringResource(R.string.cadangan_title), onBack = onBack)

            SakuCard(
                modifier = Modifier.fillMaxWidth().testTag(TestTags.CADANGAN_STATUS_CARD)
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.cadangan_status_title),
                        style = SakuTheme.text.bodyStrong,
                        color = SakuTheme.colors.text,
                    )
                    Spacer(modifier = Modifier.height(SakuSpace.tight))
                    Text(
                        text = stringResource(R.string.cadangan_transaction_count, uiState.transactionCount),
                        style = SakuTheme.text.caption,
                        color = SakuTheme.colors.textMuted,
                    )
                    Text(
                        text = uiState.lastBackupMillis?.let {
                            stringResource(
                                R.string.cadangan_last_backup,
                                formatShortDate(context, it)
                            )
                        } ?: stringResource(R.string.cadangan_never),
                        style = SakuTheme.text.caption,
                        color = SakuTheme.colors.textMuted,
                    )
                }
            }

            Column {
                SectionLabel(text = stringResource(R.string.cadangan_export))
                Spacer(modifier = Modifier.height(SakuSpace.cardInner))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SakuSpace.chipGap),
                ) {
                    SecondaryButton(
                        text = stringResource(R.string.cadangan_export_csv),
                        onClick = {
                            scope.launch {
                                val csv = viewModel.buildCsv()
                                val file = container.backupRepository.writeFile(
                                    context, "saku-cadangan.csv", csv, "text/csv"
                                )
                                viewModel.markBackupDone(file.name)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        testTag = TestTags.CADANGAN_EXPORT_CSV,
                    )
                    SecondaryButton(
                        text = stringResource(R.string.cadangan_export_json),
                        onClick = {
                            scope.launch {
                                val json = viewModel.buildJson()
                                val file = container.backupRepository.writeFile(
                                    context, "saku-cadangan.json", json, "application/json"
                                )
                                viewModel.markBackupDone(file.name)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        testTag = TestTags.CADANGAN_EXPORT_JSON,
                    )
                }
            }

            Column {
                SectionLabel(text = stringResource(R.string.cadangan_import))
                Spacer(modifier = Modifier.height(SakuSpace.cardInner))
                PrimaryButton(
                    text = if (uiState.importing) {
                        stringResource(R.string.cadangan_importing)
                    } else {
                        stringResource(R.string.cadangan_import_button)
                    },
                    onClick = {
                        importLauncher.launch(arrayOf("application/json", "text/csv", "*/*"))
                    },
                    enabled = !uiState.importing,
                    testTag = TestTags.CADANGAN_IMPORT_BUTTON,
                )
                Spacer(modifier = Modifier.height(SakuSpace.chipGap))
                HelperText(text = stringResource(R.string.cadangan_import_note))
                if (uiState.importResult != null) {
                    Spacer(modifier = Modifier.height(SakuSpace.chipGap))
                    HelperText(
                        text = stringResource(
                            R.string.cadangan_import_result,
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
                        text = stringResource(R.string.cadangan_import_error),
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
                    text = stringResource(R.string.cadangan_warning),
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
