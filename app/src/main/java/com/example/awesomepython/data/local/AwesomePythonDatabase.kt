package com.example.awesomepython.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AwesomePythonDatabase private constructor(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
), FavoriteDao {

    private val _favoritesFlow = MutableStateFlow<List<FavoriteEntity>>(emptyList())

    init {
        refreshFavorites()
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS favorites (
                name TEXT PRIMARY KEY,
                url TEXT NOT NULL,
                description TEXT NOT NULL,
                category TEXT NOT NULL,
                superGroup TEXT NOT NULL,
                subcategory TEXT NOT NULL,
                pypiPackage TEXT,
                badge TEXT,
                savedAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS favorites")
        onCreate(db)
    }

    private fun refreshFavorites() {
        val list = mutableListOf<FavoriteEntity>()
        val db = readableDatabase
        val cursor = db.query(
            "favorites",
            null,
            null,
            null,
            null,
            null,
            "savedAt DESC"
        )
        cursor.use { c ->
            val nameCol = c.getColumnIndexOrThrow("name")
            val urlCol = c.getColumnIndexOrThrow("url")
            val descCol = c.getColumnIndexOrThrow("description")
            val catCol = c.getColumnIndexOrThrow("category")
            val grpCol = c.getColumnIndexOrThrow("superGroup")
            val subCol = c.getColumnIndexOrThrow("subcategory")
            val pypiCol = c.getColumnIndexOrThrow("pypiPackage")
            val badgeCol = c.getColumnIndexOrThrow("badge")
            val savedAtCol = c.getColumnIndexOrThrow("savedAt")

            while (c.moveToNext()) {
                list.add(
                    FavoriteEntity(
                        name = c.getString(nameCol),
                        url = c.getString(urlCol),
                        description = c.getString(descCol),
                        category = c.getString(catCol),
                        superGroup = c.getString(grpCol),
                        subcategory = c.getString(subCol),
                        pypiPackage = if (c.isNull(pypiCol)) null else c.getString(pypiCol),
                        badge = if (c.isNull(badgeCol)) null else c.getString(badgeCol),
                        savedAt = c.getLong(savedAtCol)
                    )
                )
            }
        }
        _favoritesFlow.value = list
    }

    override fun getAllFavorites(): Flow<List<FavoriteEntity>> = _favoritesFlow.asStateFlow()

    override fun isFavorite(name: String): Flow<Boolean> = _favoritesFlow.map { list ->
        list.any { it.name == name }
    }

    override suspend fun insertFavorite(favorite: FavoriteEntity) = withContext(Dispatchers.IO) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", favorite.name)
            put("url", favorite.url)
            put("description", favorite.description)
            put("category", favorite.category)
            put("superGroup", favorite.superGroup)
            put("subcategory", favorite.subcategory)
            put("pypiPackage", favorite.pypiPackage)
            put("badge", favorite.badge)
            put("savedAt", favorite.savedAt)
        }
        db.insertWithOnConflict("favorites", null, values, SQLiteDatabase.CONFLICT_REPLACE)
        refreshFavorites()
    }

    override suspend fun deleteFavoriteByName(name: String) = withContext(Dispatchers.IO) {
        val db = writableDatabase
        db.delete("favorites", "name = ?", arrayOf(name))
        refreshFavorites()
    }

    fun favoriteDao(): FavoriteDao = this

    companion object {
        private const val DATABASE_NAME = "awesome_python.db"
        private const val DATABASE_VERSION = 1

        @Volatile
        private var INSTANCE: AwesomePythonDatabase? = null

        fun getDatabase(context: Context): AwesomePythonDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = AwesomePythonDatabase(context)
                INSTANCE = instance
                instance
            }
        }
    }
}
