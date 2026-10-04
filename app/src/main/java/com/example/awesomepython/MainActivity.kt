package com.example.awesomepython

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.awesomepython.data.model.NavigationTab
import com.example.awesomepython.ui.components.ProjectDetailDialog
import com.example.awesomepython.ui.screens.AboutScreen
import com.example.awesomepython.ui.screens.BookmarksScreen
import com.example.awesomepython.ui.screens.CategoriesScreen
import com.example.awesomepython.ui.screens.CategoryDetailScreen
import com.example.awesomepython.ui.screens.ExploreScreen
import com.example.awesomepython.ui.theme.AwesomePythonTheme
import com.example.awesomepython.ui.theme.PythonYellow
import com.example.awesomepython.ui.viewmodel.AwesomePythonViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    private val viewModel: AwesomePythonViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AwesomePythonTheme {
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(Unit) {
                    viewModel.toastEvents.collectLatest { message ->
                        snackbarHostState.showSnackbar(message)
                    }
                }

                AwesomePythonApp(
                    viewModel = viewModel,
                    snackbarHostState = snackbarHostState
                )
            }
        }
    }
}

@Composable
fun AwesomePythonApp(
    viewModel: AwesomePythonViewModel,
    snackbarHostState: SnackbarHostState
) {
    val uiState by viewModel.uiState.collectAsState()
    val catalog by viewModel.catalog.collectAsState()
    val filteredProjects by viewModel.filteredProjects.collectAsState()
    val favorites by viewModel.favorites.collectAsState()

    val activeCategory = uiState.activeCategoryDetail

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.systemBars,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (activeCategory == null) {
                NavigationBar(
                    modifier = Modifier.testTag("bottom_nav_bar"),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    NavigationBarItem(
                        selected = uiState.selectedTab == NavigationTab.EXPLORE,
                        onClick = { viewModel.onSelectTab(NavigationTab.EXPLORE) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = "Explore"
                            )
                        },
                        label = { Text("Explore") },
                        modifier = Modifier.testTag("nav_item_explore"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PythonYellow,
                            selectedTextColor = PythonYellow,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )

                    NavigationBarItem(
                        selected = uiState.selectedTab == NavigationTab.CATEGORIES,
                        onClick = { viewModel.onSelectTab(NavigationTab.CATEGORIES) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = "Categories"
                            )
                        },
                        label = { Text("Categories") },
                        modifier = Modifier.testTag("nav_item_categories"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PythonYellow,
                            selectedTextColor = PythonYellow,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )

                    NavigationBarItem(
                        selected = uiState.selectedTab == NavigationTab.BOOKMARKS,
                        onClick = { viewModel.onSelectTab(NavigationTab.BOOKMARKS) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = "Bookmarks"
                            )
                        },
                        label = { Text("Bookmarks") },
                        modifier = Modifier.testTag("nav_item_bookmarks"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PythonYellow,
                            selectedTextColor = PythonYellow,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )

                    NavigationBarItem(
                        selected = uiState.selectedTab == NavigationTab.ABOUT,
                        onClick = { viewModel.onSelectTab(NavigationTab.ABOUT) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "About"
                            )
                        },
                        label = { Text("About") },
                        modifier = Modifier.testTag("nav_item_about"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PythonYellow,
                            selectedTextColor = PythonYellow,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            if (activeCategory != null) {
                val categoryProjects = catalog.entries.filter {
                    it.category.equals(activeCategory.name, ignoreCase = true)
                }
                CategoryDetailScreen(
                    category = activeCategory,
                    projects = categoryProjects,
                    viewModel = viewModel,
                    onBack = { viewModel.closeCategoryDetail() },
                    onProjectClick = { viewModel.openProjectDetail(it) }
                )
            } else {
                when (uiState.selectedTab) {
                    NavigationTab.EXPLORE -> {
                        ExploreScreen(
                            viewModel = viewModel,
                            projects = filteredProjects,
                            totalProjectsCount = catalog.entries.size,
                            totalCategoriesCount = catalog.categories.size,
                            superGroups = catalog.superGroups,
                            searchQuery = uiState.searchQuery,
                            selectedGroup = uiState.selectedSuperGroup,
                            selectedCategoryName = uiState.selectedCategoryName,
                            sortOption = uiState.sortOption,
                            isLoading = uiState.isLoading,
                            onProjectClick = { viewModel.openProjectDetail(it) }
                        )
                    }

                    NavigationTab.CATEGORIES -> {
                        CategoriesScreen(
                            categories = catalog.categories,
                            allProjects = catalog.entries,
                            onCategoryClick = { category ->
                                viewModel.openCategoryDetail(category)
                            }
                        )
                    }

                    NavigationTab.BOOKMARKS -> {
                        BookmarksScreen(
                            favorites = favorites,
                            viewModel = viewModel,
                            onProjectClick = { viewModel.openProjectDetail(it) }
                        )
                    }

                    NavigationTab.ABOUT -> {
                        AboutScreen(
                            sponsors = catalog.sponsors
                        )
                    }
                }
            }
        }

        // Project Detail Modal Sheet / Dialog
        uiState.activeDetailProject?.let { project ->
            val isBookmarked = viewModel.isProjectFavorite(project.name)
            ProjectDetailDialog(
                project = project,
                isBookmarked = isBookmarked,
                onToggleBookmark = { viewModel.toggleFavorite(project) },
                onDismiss = { viewModel.closeProjectDetail() },
                onCopyMessage = { msg -> viewModel.emitToast(msg) }
            )
        }
    }
}
