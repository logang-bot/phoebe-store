package com.example.phoebestore.data.repository.impl

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.phoebestore.domain.repository.UserSettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userSettingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_settings"
)

@Singleton
class UserSettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UserSettingsRepository {

    override val lastAccessedStoreId: Flow<String?> = context.userSettingsDataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs -> prefs[KEY_LAST_STORE_ID] }

    override suspend fun setLastAccessedStore(storeId: String) {
        context.userSettingsDataStore.edit { prefs ->
            prefs[KEY_LAST_STORE_ID] = storeId
        }
    }

    companion object {
        private val KEY_LAST_STORE_ID = stringPreferencesKey("last_accessed_store_id")
    }
}
