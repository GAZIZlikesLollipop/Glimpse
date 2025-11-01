package org.app.glimpse.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.app.glimpse.Route

interface UserPreferences {
    val token: Flow<String>
    val startRoute: Flow<String>
    val userLang: Flow<String>
    suspend fun setToken(token: String)
    suspend fun setStartRoute(route: String)
    suspend fun setUserLang(lang: String)
}

val Context.userPreferences by preferencesDataStore("userPreferences")

class UserPreferencesRepository(context: Context): UserPreferences {
    val dataStore = context.userPreferences
    override val token: Flow<String> = dataStore.data.map {
        it[stringPreferencesKey("token")] ?: ""
    }

    override val startRoute: Flow<String> = dataStore.data.map {
        it[stringPreferencesKey("start_route")] ?: Route.Login.route
    }

    override val userLang: Flow<String> = dataStore.data.map {
        it[stringPreferencesKey("user_lang")] ?: "en"
    }

    override suspend fun setUserLang(lang: String) {
        dataStore.edit {
            it[stringPreferencesKey("user_lang")] = lang
        }
    }

    override suspend fun setToken(token: String) {
        dataStore.edit {
            it[stringPreferencesKey("token")] = token
        }
    }

    override suspend fun setStartRoute(route: String) {
        dataStore.edit {
            it[stringPreferencesKey("start_route")] = route
        }
    }

}