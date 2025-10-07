package org.muilab.noti.summary.database.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.muilab.noti.summary.model.APIKeyEntity


@Dao
interface APIKeyDao {
    @Query("SELECT * FROM api_key_pool")
    fun getAllAPI(): Flow<List<APIKeyEntity>>

    @Query("SELECT * FROM api_key_pool")
    fun getAllAPIStatic(): List<APIKeyEntity>

    @Query("SELECT * FROM api_key_pool WHERE APIKey = :apiKey")
    fun getAPIKeyByAPI(apiKey: String): APIKeyEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAPIKey(api: APIKeyEntity)

    @Transaction
    fun insertAPIKeyIfNotExists(api: APIKeyEntity) {
        val existingAPI = getAPIKeyByAPI(apiKey = api.APIKey)
        if (existingAPI == null) {
            insertAPIKey(api)
        }
    }

    @Update
    fun update(apiKey: APIKeyEntity)

    @Query("DELETE FROM api_key_pool WHERE APIKey = :api")
    fun deleteByAPIKey(api: String)

    @Query("DELETE FROM api_key_pool")
    fun deleteAllAPIKey()
}