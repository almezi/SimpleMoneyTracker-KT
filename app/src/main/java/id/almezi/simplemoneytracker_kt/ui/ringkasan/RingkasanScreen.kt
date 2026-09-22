package id.almezi.simplemoneytracker_kt.ui.ringkasan

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.almezi.simplemoneytracker_kt.R
import id.almezi.simplemoneytracker_kt.SimpleMoneyTrackerApp
import id.almezi.simplemoneytracker_kt.TestTags
import id.almezi.simplemoneytracker_kt.ui.getCategoryNameRes

@Composable
fun RingkasanScreen(
    modifier: Modifier = Modifier,
    viewModel: RingkasanViewModel = viewModel(
        factory = RingkasanViewModel.Factory(
            (LocalContext.current.applicationContext as SimpleMoneyTrackerApp).container.transactionRepository
        )
    )
) {
    val summary by viewModel.summary.collectAsState()

    Scaffold(
        modifier = modifier.testTag(TestTags.RINGKASAN_SCREEN),
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            when (summary) {
                is RingkasanViewModel.SummaryState.Loading -> {
                    item {
                        Text(
                            text = stringResource(R.string.loading),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                is RingkasanViewModel.SummaryState.Success -> {
                    val successState = summary as RingkasanViewModel.SummaryState.Success

                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .testTag(TestTags.RINGKASAN_TOTALS_CARD)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = stringResource(R.string.tambah_type_pemasukan),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = successState.totalPemasukan.toString(),
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                Spacer(modifier = Modifier.padding(8.dp))
                                Text(
                                    text = stringResource(R.string.tambah_type_pengeluaran),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = successState.totalPengeluaran.toString(),
                                    style = MaterialTheme.typography.headlineMedium
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = stringResource(R.string.tambah_type_pemasukan),
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    items(successState.pemasukanByCategory.toList()) { (category, amount) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .testTag(TestTags.RINGKASAN_CATEGORY_ITEM)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = stringResource(getCategoryNameRes(category)),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = amount.toString(),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = stringResource(R.string.tambah_type_pengeluaran),
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    items(successState.pengeluaranByCategory.toList()) { (category, amount) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .testTag(TestTags.RINGKASAN_CATEGORY_ITEM)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = amount.toString(),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
