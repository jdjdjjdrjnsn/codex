package com.codex.cleaner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.codex.cleaner.data.AndroidCleanerRepository
import com.codex.cleaner.domain.CleanableItem
import com.codex.cleaner.ui.CleanerViewModel
import com.codex.cleaner.ui.CleanerViewModelFactory
import com.codex.cleaner.ui.CleanerUiState
import java.text.DecimalFormat

class MainActivity : ComponentActivity() {

    private val viewModel: CleanerViewModel by viewModels {
        CleanerViewModelFactory(AndroidCleanerRepository(applicationContext))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val state by viewModel.uiState.collectAsState()
                    CleanerScreen(
                        state = state,
                        onScan = viewModel::scanNow,
                        onClean = viewModel::cleanSelected,
                        onToggleItem = viewModel::toggleItem
                    )
                }
            }
        }
    }
}

@Composable
private fun CleanerScreen(
    state: CleanerUiState,
    onScan: () -> Unit,
    onClean: () -> Unit,
    onToggleItem: (String, Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Android Telefon Temizleyici", style = MaterialTheme.typography.headlineSmall)
        Text("Açılabilir alan: ${formatBytes(state.estimatedReclaimableBytes)}")

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onScan, enabled = !state.isScanning) {
                Text(if (state.isScanning) "Taranıyor..." else "Şimdi Tara")
            }
            Button(onClick = onClean, enabled = !state.isCleaning && state.items.any { it.isSelected }) {
                Text(if (state.isCleaning) "Temizleniyor..." else "Hızlı Temizlik")
            }
        }

        if (state.isScanning || state.isCleaning) {
            CircularProgressIndicator()
        }

        state.summary?.let {
            Text("Temizlik tamamlandı. Kazanılan alan: ${formatBytes(it.cleanedBytes)}")
        }

        state.errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.items, key = { it.id }) { item ->
                CleanerItemRow(item = item, onToggle = onToggleItem)
            }
        }
    }
}

@Composable
private fun CleanerItemRow(item: CleanableItem, onToggle: (String, Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.name, style = MaterialTheme.typography.titleSmall)
            Text(item.category.name + " • " + item.riskLevel.name + " • " + formatBytes(item.sizeBytes))
            Text(item.path, style = MaterialTheme.typography.bodySmall)
        }
        Checkbox(checked = item.isSelected, onCheckedChange = { onToggle(item.id, it) })
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB")
    var value = bytes.toDouble()
    var unitIndex = 0
    while (value >= 1024 && unitIndex < units.lastIndex) {
        value /= 1024
        unitIndex++
    }
    return "${DecimalFormat("#.##").format(value)} ${units[unitIndex]}"
}
