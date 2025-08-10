package org.app.glimpse.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.app.glimpse.jwtPreferences

interface JWTPreferences {
    val token: Flow<String>
    suspend fun setToken(token: String)
}

class JWTRepository(context: Context): JWTPreferences {
    val dataStore = context.jwtPreferences
    override val token: Flow<String> = dataStore.data.map {
        it[stringPreferencesKey("TOKEN")] ?: ""
    }

    override suspend fun setToken(token: String) {
        dataStore.edit {
            it[stringPreferencesKey("TOKEN")] = token
        }
    }
}