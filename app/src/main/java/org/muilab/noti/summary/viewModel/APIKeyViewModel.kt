package org.muilab.noti.summary.viewModel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.muilab.noti.summary.R
import org.muilab.noti.summary.database.room.APIKeyDatabase
import org.muilab.noti.summary.model.APIKeyEntity

class APIKeyViewModel(application: Application, apiKeyDatabase: APIKeyDatabase) :
    AndroidViewModel(application) {

    private val sharedPreferences =
        getApplication<Application>().getSharedPreferences("ApiPref", Context.MODE_PRIVATE)

    private val apiKeyDao = apiKeyDatabase.apiKeyDao()

    @SuppressLint("StaticFieldLeak")
    private val context = getApplication<Application>().applicationContext

    private val keyNotProvided = context.getString(R.string.key_not_provided)
    private val _apiKey = MutableLiveData<APIKeyEntity>()
    val apiKey: LiveData<APIKeyEntity> = _apiKey
    val allAPIKey: LiveData<List<APIKeyEntity>> = apiKeyDao.getAllAPI().asLiveData()

    private val scope = viewModelScope + Dispatchers.IO

    init {
        scope.launch {
            val resultValue = sharedPreferences.getString("userAPIKey", keyNotProvided)
            if (resultValue != keyNotProvided) {
                val apiKeyEntity = apiKeyDao.getAPIKeyByAPI(resultValue!!)
                if (apiKeyEntity != null) {
                    _apiKey.postValue(apiKeyEntity)
                } else {
                    // Handle case where the key is in shared prefs but not in the DB
                    val firstKey = apiKeyDao.getAllAPIStatic().firstOrNull()
                    if (firstKey != null) {
                        _apiKey.postValue(firstKey)
                        sharedPreferences.edit().putString("userAPIKey", firstKey.APIKey).apply()
                    }
                }
            }
        }
    }

    fun addAPI(newApiKey: String, baseUrl: String, model: String) {
        val trimmedApiKey = newApiKey.trim()
        val apiKeyEntity = APIKeyEntity(APIKey = trimmedApiKey, baseUrl = baseUrl, model = model)
        scope.launch {
            apiKeyDao.insertAPIKeyIfNotExists(apiKeyEntity)
        }
        chooseAPI(apiKeyEntity)
    }

    fun updateAPI(apiKeyEntity: APIKeyEntity) {
        scope.launch {
            apiKeyDao.update(apiKeyEntity)
        }
        chooseAPI(apiKeyEntity)
    }

    fun chooseAPI(updateAPIKey: APIKeyEntity) {
        if (_apiKey.value != updateAPIKey) {
            _apiKey.postValue(updateAPIKey)
            if (updateAPIKey.APIKey == context.getString(R.string.key_not_provided))
                sharedPreferences.edit().remove("userAPIKey").apply()
            else
                sharedPreferences.edit().putString("userAPIKey", updateAPIKey.APIKey).apply()
        }
    }

    fun deleteAPI(apiKey: APIKeyEntity) {
        if (allAPIKey.value?.size!! > 1) {
            scope.launch {
                apiKeyDao.deleteByAPIKey(apiKey.APIKey)
                chooseAPI(apiKeyDao.getAllAPIStatic()[0])
            }
        }
    }
}
