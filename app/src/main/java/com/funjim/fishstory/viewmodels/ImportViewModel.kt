package com.funjim.fishstory.viewmodels

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.funjim.fishstory.repository.FishStoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream

class ImportViewModel(
    private val repository: FishStoryRepository
) : ViewModel() {
    var isImporting by mutableStateOf(false)

    fun startImport(inputStream: InputStream) {
        viewModelScope.launch(Dispatchers.IO) {
            isImporting = true
            try {
                // Your CSV parsing logic here
                repository.importFromCsv(inputStream)
                // Show a success message or navigate back
            } catch (e: Exception) {
                // Handle errors (e.g., bad date format)
            } finally {
                isImporting = false
            }
        }
    }
    suspend fun importDatabaseFromCSV(context: Context, uri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    startImport(inputStream)
                }
                // For simplicity, assume success, but you should check deserialization status
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

}

class ImportViewModelFactory(
    private val repository: FishStoryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ImportViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ImportViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
