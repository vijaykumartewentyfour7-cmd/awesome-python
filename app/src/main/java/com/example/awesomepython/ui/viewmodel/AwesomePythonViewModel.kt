package com.example.awesomepython.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.awesomepython.data.local.FavoriteEntity
import com.example.awesomepython.data.model.AwesomePythonCatalog
import com.example.awesomepython.data.model.Category
import com.example.awesomepython.data.model.NavigationTab
import com.example.awesomepython.data.model.PythonProject
import com.example.awesomepython.data.model.SortOption
import com.example.awesomepython.data.repository.PythonLibraryRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UiState(
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val selectedSuperGroup: String = "All",
    val selectedCategoryName: String? = null,
    val sortOption: SortOption = SortOption.NAME_ASC,
    val selectedTab: NavigationTab = NavigationTab.EXPLORE,
    val activeDetailProject: PythonProject? = null,
    val activeCategoryDetail: Category? = null
)

class AwesomePythonViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PythonLibraryRepository(application)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _catalog = MutableStateFlow(AwesomePythonCatalog(emptyList(), emptyList(), emptyList(), emptyList()))
    val catalog: StateFlow<AwesomePythonCatalog> = _catalog.asStateFlow()

    val favorites: StateFlow<List<FavoriteEntity>> = repository.getAllFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _toastEvents = MutableSharedFlow<String>()
    val toastEvents: SharedFlow<String> = _toastEvents.asSharedFlow()

    init {
        loadCatalog()
    }

    private fun loadCatalog() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val data = repository.getCatalog()
                _catalog.value = data
            } catch (e: Exception) {
                _toastEvents.emit("Failed to load catalog: ${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    val filteredProjects: StateFlow<List<PythonProject>> = combine(
        _catalog,
        _uiState
    ) { cat, state ->
        var list = cat.entries.asSequence()

        // Filter by super group if selected
        if (state.selectedSuperGroup != "All") {
            list = list.filter { it.superGroup.equals(state.selectedSuperGroup, ignoreCase = true) }
        }

        // Filter by category if selected
        if (state.selectedCategoryName != null) {
            list = list.filter { it.category.equals(state.selectedCategoryName, ignoreCase = true) }
        }

        // Filter by search query
        if (state.searchQuery.isNotBlank()) {
            val q = state.searchQuery.trim().lowercase()
            list = list.filter {
                it.name.lowercase().contains(q) ||
                it.description.lowercase().contains(q) ||
                it.category.lowercase().contains(q) ||
                it.subcategory.lowercase().contains(q) ||
                (it.pypiPackage != null && it.pypiPackage.lowercase().contains(q))
            }
        }

        // Apply sorting
        val sortedList = when (state.sortOption) {
            SortOption.NAME_ASC -> list.sortedBy { it.name.lowercase() }.toList()
            SortOption.NAME_DESC -> list.sortedByDescending { it.name.lowercase() }.toList()
            SortOption.CATEGORY -> list.sortedWith(compareBy({ it.category.lowercase() }, { it.name.lowercase() })).toList()
        }

        sortedList
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun onSelectSuperGroup(group: String) {
        _uiState.value = _uiState.value.copy(
            selectedSuperGroup = group,
            selectedCategoryName = null
        )
    }

    fun onSelectCategoryFilter(categoryName: String?) {
        _uiState.value = _uiState.value.copy(selectedCategoryName = categoryName)
    }

    fun onSortOptionChange(sort: SortOption) {
        _uiState.value = _uiState.value.copy(sortOption = sort)
    }

    fun onSelectTab(tab: NavigationTab) {
        _uiState.value = _uiState.value.copy(
            selectedTab = tab,
            activeCategoryDetail = null
        )
    }

    fun openProjectDetail(project: PythonProject) {
        _uiState.value = _uiState.value.copy(activeDetailProject = project)
    }

    fun closeProjectDetail() {
        _uiState.value = _uiState.value.copy(activeDetailProject = null)
    }

    fun openCategoryDetail(category: Category) {
        _uiState.value = _uiState.value.copy(activeCategoryDetail = category)
    }

    fun closeCategoryDetail() {
        _uiState.value = _uiState.value.copy(activeCategoryDetail = null)
    }

    fun toggleFavorite(project: PythonProject) {
        viewModelScope.launch {
            val isCurrentlyFav = favorites.value.any { it.name == project.name }
            repository.toggleFavorite(project, isCurrentlyFav)
            val msg = if (isCurrentlyFav) {
                "Removed ${project.name} from bookmarks"
            } else {
                "Saved ${project.name} to bookmarks"
            }
            _toastEvents.emit(msg)
        }
    }

    fun isProjectFavorite(projectName: String): Boolean {
        return favorites.value.any { it.name == projectName }
    }

    fun emitToast(msg: String) {
        viewModelScope.launch {
            _toastEvents.emit(msg)
        }
    }
}
