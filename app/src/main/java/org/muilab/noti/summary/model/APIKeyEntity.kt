package org.muilab.noti.summary.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "api_key_pool")
data class APIKeyEntity(
    @PrimaryKey(autoGenerate = true)
    var primaryKey: Int = 0,
    var APIKey: String,
    var baseUrl: String = "https://api.openai.com/v1/",
    var model: String = "gpt-3.5-turbo",
)
