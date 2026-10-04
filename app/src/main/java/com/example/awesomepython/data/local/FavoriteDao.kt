package com.example.awesomepython.data.local

import kotlinx.coroutines.flow.Flow

interface FavoriteDao {
    fun getAllFavorites(): Flow<List<FavoriteEntity>>
    fun isFavorite(name: String): Flow<Boolean>
    suspend fun insertFavorite(favorite: FavoriteEntity)
    suspend fun deleteFavoriteByName(name: String)
}
