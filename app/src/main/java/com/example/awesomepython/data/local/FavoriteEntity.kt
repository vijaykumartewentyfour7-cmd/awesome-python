package com.example.awesomepython.data.local

import com.example.awesomepython.data.model.PythonProject

data class FavoriteEntity(
    val name: String,
    val url: String,
    val description: String,
    val category: String,
    val superGroup: String,
    val subcategory: String,
    val pypiPackage: String?,
    val badge: String?,
    val savedAt: Long
)

fun FavoriteEntity.toProject(): PythonProject {
    return PythonProject(
        name = name,
        url = url,
        description = description,
        category = category,
        superGroup = superGroup,
        subcategory = subcategory,
        pypiPackage = pypiPackage,
        badge = badge
    )
}

fun projectToFavorite(project: PythonProject): FavoriteEntity {
    return FavoriteEntity(
        name = project.name,
        url = project.url,
        description = project.description,
        category = project.category,
        superGroup = project.superGroup,
        subcategory = project.subcategory,
        pypiPackage = project.pypiPackage,
        badge = project.badge,
        savedAt = System.currentTimeMillis()
    )
}
