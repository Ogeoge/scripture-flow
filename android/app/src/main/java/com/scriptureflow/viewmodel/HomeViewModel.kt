package com.scriptureflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.scriptureflow.core.model.Verse
import com.scriptureflow.data.repo.ScriptureRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: ScriptureRepository,
) : ViewModel() {

    sealed class UiState {
        data object Loading : UiState()

        data class Ready(
            val verse: Verse,
            val isHighlighted: Boolean,
            val lastErrorMessage: String? = null,
        ) : UiState()
    }

    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val verse = repository.getVerseOfTheDay()
                val highlighted = repository.isHighlighted(verse.id)
                _uiState.value = UiState.Ready(
                    verse = verse,
                    isHighlighted = highlighted,
                    lastErrorMessage = null,
                )
            } catch (t: Throwable) {
                // Keep minimal; surface a simple message.
                _uiState.value = UiState.Ready(
                    verse = Verse(
                        id = "error|0|0",
                        ref = com.scriptureflow.core.model.VerseRef(book = "Error", chapter = 0, verse = 0),
                        text = "Unable to load Verse of the Day.",
                    ),
                    isHighlighted = false,
                    lastErrorMessage = (t.message ?: "Unknown error"),
                )
            }
        }
    }

    fun toggleHighlightForVerseOfDay() {
        val current = _uiState.value
        if (current !is UiState.Ready) return

        viewModelScope.launch {
            try {
                if (current.isHighlighted) {
                    repository.clearHighlight(current.verse.id)
                    _uiState.update {
                        (it as UiState.Ready).copy(isHighlighted = false, lastErrorMessage = null)
                    }
                } else {
                    // Minimal default highlight color: soft yellow.
                    // ARGB packed int.
                    val colorArgb = 0xFFFFF59D.toInt()
                    repository.setHighlight(current.verse.id, colorArgb)
                    _uiState.update {
                        (it as UiState.Ready).copy(isHighlighted = true, lastErrorMessage = null)
                    }
                }
            } catch (t: Throwable) {
                _uiState.update {
                    val ready = it as? UiState.Ready
                    ready?.copy(lastErrorMessage = t.message ?: "Unable to update highlight")
                        ?: UiState.Loading
                }
            }
        }
    }

    class Factory(
        private val repository: ScriptureRepository,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HomeViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
