package com.codex.cleaner.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.codex.cleaner.data.CleanerRepository
import com.codex.cleaner.domain.CleanableItem
import com.codex.cleaner.domain.CleaningSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CleanerUiState(
    val isScanning: Boolean = false,
    val isCleaning: Boolean = false,
    val items: List<CleanableItem> = emptyList(),
    val summary: CleaningSummary? = null,
    val errorMessage: String? = null
) {
    val estimatedReclaimableBytes: Long get() = items.filter { it.isSelected }.sumOf { it.sizeBytes }
}

class CleanerViewModel(
    private val repository: CleanerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CleanerUiState())
    val uiState: StateFlow<CleanerUiState> = _uiState.asStateFlow()

    fun scanNow() {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true, errorMessage = null) }
            runCatching { repository.scan() }
                .onSuccess { items ->
                    _uiState.update { it.copy(isScanning = false, items = items, summary = null) }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isScanning = false,
                            errorMessage = throwable.message ?: "Tarama sırasında hata oluştu"
                        )
                    }
                }
        }
    }

    fun toggleItem(itemId: String, selected: Boolean) {
        _uiState.update { state ->
            state.copy(items = state.items.map { if (it.id == itemId) it.copy(isSelected = selected) else it })
        }
    }

    fun cleanSelected() {
        viewModelScope.launch {
            val selectedItems = uiState.value.items.filter { it.isSelected }
            if (selectedItems.isEmpty()) return@launch

            _uiState.update { it.copy(isCleaning = true, errorMessage = null) }
            runCatching { repository.delete(selectedItems) }
                .onSuccess { summary ->
                    _uiState.update { state ->
                        state.copy(
                            isCleaning = false,
                            summary = summary,
                            items = state.items.filterNot { it.isSelected }
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isCleaning = false,
                            errorMessage = throwable.message ?: "Temizleme sırasında hata oluştu"
                        )
                    }
                }
        }
    }
}

class CleanerViewModelFactory(
    private val repository: CleanerRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CleanerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CleanerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
