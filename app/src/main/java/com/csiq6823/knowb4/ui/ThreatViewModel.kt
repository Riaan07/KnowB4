package com.csiq6823.knowb4.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.csiq6823.knowb4.Data.ThreatRepository
import com.csiq6823.knowb4.Data.ThreatScan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThreatViewModel(private val repository: ThreatRepository) : ViewModel() {

    val allScans: StateFlow<List<ThreatScan>> = repository.allScans.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun insertSharedText(sharedText: String, sourceApp: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.processAndInsertScan(sharedText, sourceApp)
        }
    }

    fun clearHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearHistory()
        }
    }

    class ThreatViewModelFactory(private val repository: ThreatRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ThreatViewModel::class.java)) {
                return ThreatViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
