package id.almezi.simplemoneytracker_kt.ui.daftar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.almezi.simplemoneytracker_kt.R
import id.almezi.simplemoneytracker_kt.SimpleMoneyTrackerApp
import id.almezi.simplemoneytracker_kt.TestTags
import id.almezi.simplemoneytracker_kt.data.Transaction
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun DaftarScreen(
    modifier: Modifier = Modifier,
    viewModel: DaftarViewModel = viewModel(
        factory = DaftarViewModel.Factory(
            (LocalContext.current.applicationContext as SimpleMoneyTrackerApp).container.transactionRepository
        )
    )
) {
    val transactions by viewModel.transactions.collectAsState()
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))

    Scaffold(
        modifier = modifier.testTag(TestTags.DAFTAR_SCREEN),
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(transactions) { transaction ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .testTag(TestTags.DAFTAR_ITEM)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = dateFormat.format(transaction.dateEpochMillis),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.testTag(TestTags.DAFTAR_ITEM_DATE)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = transaction.amount.toString(),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.testTag(TestTags.DAFTAR_ITEM_AMOUNT)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = transaction.categoryId,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.testTag(TestTags.DAFTAR_ITEM_CATEGORY)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = { /* TODO: Implement delete */ },
                                modifier = Modifier.testTag(TestTags.DAFTAR_ITEM_DELETE)
                            ) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.delete)
                                )
                            }
                        }
                        if (transaction.note != null) {
                            Text(
                                text = transaction.note,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.testTag(TestTags.DAFTAR_ITEM_NOTE)
                            )
                        }
                    }
                }
            }
        }
    }
}
