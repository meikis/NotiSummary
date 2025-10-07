package org.muilab.noti.summary.database.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.muilab.noti.summary.model.APIKeyEntity

@Database(entities = [APIKeyEntity::class], version = 2)
abstract class APIKeyDatabase : RoomDatabase() {

    abstract fun apiKeyDao(): APIKeyDao

    companion object {
        @Volatile
        private var INSTANCE: APIKeyDatabase? = null

        fun getInstance(context: Context): APIKeyDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE api_key_pool ADD COLUMN baseUrl TEXT NOT NULL DEFAULT 'https://api.openai.com/v1/'")
                database.execSQL("ALTER TABLE api_key_pool ADD COLUMN model TEXT NOT NULL DEFAULT 'gpt-3.5-turbo'")
            }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                APIKeyDatabase::class.java,
                "api_key_pool"
            )
                .addMigrations(MIGRATION_1_2)
                .build()
    }
}
