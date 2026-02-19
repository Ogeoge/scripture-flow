package com.scriptureflow.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scriptureflow.data.repo.LastLocation
import com.scriptureflow.data.repo.ScriptureRepository
import com.scriptureflow.domain.model.VerseRef
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * UI state for the Home screen.
 *
 * @param isLoading Indicates if the initial data (like Verse of the Day) is being loaded.
 * @param lastLocation The user's last reading location for the "Continue Reading" card.
 * @param verseOfTheDay The data for the "Verse of the Day" card.
 */
data class HomeUiState(
    val isLoading: Boolean = true,
    val lastLocation: LastLocation? = null,
    val verseOfTheDay: VerseOfTheDayUiModel? = null
)

/**
 * A display-ready model for the Verse of the Day.
 *
 * @param ref The stable reference to the verse.
 * @param text The full text of the verse.
 * @param bookName The display name of the book (e.g., "Genesis").
 */
data class VerseOfTheDayUiModel(
    val ref: VerseRef,
    val text: String,
    val bookName: String
)

/**
 * ViewModel for the Home screen.
 *
 * This ViewModel is responsible for loading the data required for the home screen,
 * including the user's last reading location and the verse of the day.
 * It exposes this data as a [HomeUiState] flow.
 */
class HomeViewModel(
    private val scriptureRepository: ScriptureRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // Observe last location changes from the database.
        viewModelScope.launch {
            scriptureRepository.getLastLocation().collect { location ->
                _uiState.update { it.copy(lastLocation = location) }
            }
        }

        // Perform a one-time load for the Verse of the Day.
        loadVerseOfTheDay()
    }

    private fun loadVerseOfTheDay() {
        viewModelScope.launch {
            try {
                // Per contract, timezone is specified in the asset. We use it for calculation.
                // The contract example uses 'UTC', which is a safe default.
                val timezoneId = ZoneId.of("UTC") // In a real app, this could come from repo
                val now = ZonedDateTime.now(timezoneId)
                val dayOfYear = now.dayOfYear

                val votdRef = scriptureRepository.getVerseRefForDayOfYear(dayOfYear)

                if (votdRef != null) {
                    // To get the verse text and book name, we need to load the assets.
                    val book = scriptureRepository.getBooks().find { it.bookId == votdRef.bookId }
                    val chapter = scriptureRepository.getChapter(votdRef.bookId, votdRef.chapter)
                    val verse = chapter?.verses?.find { it.verse == votdRef.verse }

                    if (book != null && verse != null) {
                        val votdModel = VerseOfTheDayUiModel(
                            ref = votdRef,
                            text = verse.text,
                            bookName = book.name
                        )
                        _uiState.update { it.copy(verseOfTheDay = votdModel) }
                    }
                }
            } catch (e: Exception) {
                // TODO: Log the exception
                // Failed to load VOTD, update state to reflect this if necessary.
            } finally {
                // Regardless of success or failure, stop the loading indicator.
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
