package com.example.awesomepython.data.model

data class PythonProject(
    val name: String,
    val url: String,
    val description: String,
    val category: String,
    val superGroup: String,
    val subcategory: String,
    val pypiPackage: String? = null,
    val badge: String? = null
)

data class Category(
    val name: String,
    val slug: String,
    val group: String,
    val description: String = "",
    val intro: String = ""
)

data class Sponsor(
    val name: String,
    val url: String,
    val description: String
)

data class AwesomePythonCatalog(
    val superGroups: List<String>,
    val categories: List<Category>,
    val entries: List<PythonProject>,
    val sponsors: List<Sponsor>
)

enum class SortOption(val label: String) {
    NAME_ASC("Name (A–Z)"),
    NAME_DESC("Name (Z–A)"),
    CATEGORY("Category")
}

enum class NavigationTab(val label: String) {
    EXPLORE("Explore"),
    CATEGORIES("Categories"),
    BOOKMARKS("Bookmarks"),
    ABOUT("About")
}
