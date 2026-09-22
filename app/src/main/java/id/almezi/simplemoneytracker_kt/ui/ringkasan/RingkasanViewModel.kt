package id.almezi.simplemoneytracker_kt.ui.ringkasan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import id.almezi.simplemoneytracker_kt.data.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RingkasanViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _summary = MutableStateFlow<SummaryState>(SummaryState.Loading)
    val summary: StateFlow<SummaryState> = _summary.asStateFlow()

    init {
        loadSummary()
    }

    private fun loadSummary() {
        viewModelScope.launch {
            repository.getAllTransactions().collect { transactions ->
                val pemasukan = transactions.filter { it.type == "pemasukan" }
                val pengeluaran = transactions.filter { it.type == "pengeluaran" }

                val pemasukanByCategory = pemasukan.groupBy { it.categoryId }
                    .mapValues { (_, txs) -> txs.sumOf { it.amount } }

                val pengeluaranByCategory = pengeluaran.groupBy { it.categoryId }
                    .mapValues { (_, txs) -> txs.sumOf { it.amount } }

                _summary.value = SummaryState.Success(
                    totalPemasukan = pemasukan.sumOf { it.amount },
                    totalPengeluaran = pengeluaran.sumOf { it.amount },
                    pemasukanByCategory = pemasukanByCategory,
                    pengeluaranByCategory = pengeluaranByCategory
                )
            }
        }
    }

    sealed class SummaryState {
        object Loading : SummaryState()
        data class Success(
            val totalPemasukan: Long,
            val totalPengeluaran: Long,
            val pemasukanByCategory: Map<String, Long>,
            val pengeluaranByCategory: Map<String, Long>
        ) : SummaryState()
    }

    class Factory(
        private val repository: TransactionRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RingkasanViewModel::class.java)) {
                return RingkasanViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
