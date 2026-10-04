package com.example.awesomepython.data.repository

import android.content.Context
import com.example.awesomepython.data.local.AwesomePythonDatabase
import com.example.awesomepython.data.local.FavoriteEntity
import com.example.awesomepython.data.local.projectToFavorite
import com.example.awesomepython.data.model.AwesomePythonCatalog
import com.example.awesomepython.data.model.Category
import com.example.awesomepython.data.model.PythonProject
import com.example.awesomepython.data.model.Sponsor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONObject

class PythonLibraryRepository(private val context: Context) {

    private val db = AwesomePythonDatabase.getDatabase(context)
    private val favoriteDao = db.favoriteDao()

    @Volatile
    private var cachedCatalog: AwesomePythonCatalog? = null

    suspend fun getCatalog(): AwesomePythonCatalog = withContext(Dispatchers.IO) {
        cachedCatalog?.let { return@withContext it }

        val jsonString = context.assets.open("awesome_python_data.json").bufferedReader().use {
            it.readText()
        }
        val rootObj = JSONObject(jsonString)

        val superGroupsJson = rootObj.getJSONArray("superGroups")
        val superGroups = mutableListOf<String>()
        for (i in 0 until superGroupsJson.length()) {
            superGroups.add(superGroupsJson.getString(i))
        }

        val categoriesJson = rootObj.getJSONArray("categories")
        val categories = mutableListOf<Category>()
        for (i in 0 until categoriesJson.length()) {
            val cObj = categoriesJson.getJSONObject(i)
            categories.add(
                Category(
                    name = cObj.getString("name"),
                    slug = cObj.getString("slug"),
                    group = cObj.optString("group", ""),
                    description = cObj.optString("description", ""),
                    intro = cObj.optString("intro", "")
                )
            )
        }

        val entriesJson = rootObj.getJSONArray("entries")
        val entries = mutableListOf<PythonProject>()
        for (i in 0 until entriesJson.length()) {
            val eObj = entriesJson.getJSONObject(i)
            entries.add(
                PythonProject(
                    name = eObj.getString("name"),
                    url = eObj.getString("url"),
                    description = eObj.getString("description"),
                    category = eObj.getString("category"),
                    superGroup = eObj.optString("superGroup", ""),
                    subcategory = eObj.optString("subcategory", "General"),
                    pypiPackage = if (eObj.has("pypiPackage") && !eObj.isNull("pypiPackage")) eObj.getString("pypiPackage") else null,
                    badge = if (eObj.has("badge") && !eObj.isNull("badge")) eObj.getString("badge") else null
                )
            )
        }

        val sponsorsJson = rootObj.optJSONArray("sponsors")
        val sponsors = mutableListOf<Sponsor>()
        if (sponsorsJson != null) {
            for (i in 0 until sponsorsJson.length()) {
                val sObj = sponsorsJson.getJSONObject(i)
                sponsors.add(
                    Sponsor(
                        name = sObj.getString("name"),
                        url = sObj.getString("url"),
                        description = sObj.getString("description")
                    )
                )
            }
        }

        val catalog = AwesomePythonCatalog(
            superGroups = superGroups,
            categories = categories,
            entries = entries,
            sponsors = sponsors
        )
        cachedCatalog = catalog
        catalog
    }

    fun getAllFavorites(): Flow<List<FavoriteEntity>> = favoriteDao.getAllFavorites()

    fun isFavorite(name: String): Flow<Boolean> = favoriteDao.isFavorite(name)

    suspend fun toggleFavorite(project: PythonProject, currentIsFav: Boolean) = withContext(Dispatchers.IO) {
        if (currentIsFav) {
            favoriteDao.deleteFavoriteByName(project.name)
        } else {
            favoriteDao.insertFavorite(projectToFavorite(project))
        }
    }
}
