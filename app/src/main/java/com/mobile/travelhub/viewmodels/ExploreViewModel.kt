package com.mobile.travelhub.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.data.SearchHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExploreUiState(
    val recentSearches: List<String> = emptyList()
)

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val searchHistoryRepository: SearchHistoryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        ExploreUiState(recentSearches = searchHistoryRepository.recentSearches.value)
    )
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    init {
        searchHistoryRepository.refresh()
        viewModelScope.launch {
            searchHistoryRepository.recentSearches.collect { recentSearches ->
                _uiState.update { it.copy(recentSearches = recentSearches) }
            }
        }
    }
}
