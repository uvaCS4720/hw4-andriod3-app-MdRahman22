package edu.nd.pmcburne.hello

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class MainUiState(
    val locations: List<LocationEntity> = emptyList(),
    val selectedTag: String = "core",
    val isLoading: Boolean = true
) {
    val availableTags: List<String>
        get() = locations
            .flatMap { location ->
                if (location.tags.isBlank()) {
                    emptyList()
                } else {
                    location.tags.split(",").map { it.trim() }
                }
            }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()

    val filteredLocations: List<LocationEntity>
        get() = locations.filter { location ->
            location.tags
                .split(",")
                .map { it.trim() }
                .any { it.equals(selectedTag, ignoreCase = true) }
        }
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CampusRepo

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = CampusRepo(database.locationDao())
        loadLocations()
    }

    fun loadLocations() {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(isLoading = true)
            }

            val locations = withContext(Dispatchers.IO) {
                repository.refreshLocations()
            }

            val tags = locations
                .flatMap { location ->
                    location.tags.split(",").map { it.trim() }
                }
                .filter { it.isNotBlank() }
                .distinct()
                .sorted()

            val nextSelectedTag = when {
                tags.contains(_uiState.value.selectedTag) -> _uiState.value.selectedTag
                tags.contains("core") -> "core"
                tags.isNotEmpty() -> tags.first()
                else -> "core"
            }

            _uiState.update { currentState ->
                currentState.copy(
                    locations = locations,
                    selectedTag = nextSelectedTag,
                    isLoading = false
                )
            }
        }
    }

    fun selectTag(tag: String) {
        _uiState.update { currentState ->
            currentState.copy(selectedTag = tag)
        }
    }
}