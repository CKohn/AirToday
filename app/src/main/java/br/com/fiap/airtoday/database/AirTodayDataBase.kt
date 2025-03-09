package br.com.fiap.airtoday.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import br.com.fiap.airtoday.dao.AirTodayDao
import br.com.fiap.airtoday.model.AirToday

@Database(entities = [AirToday::class], version = 1, exportSchema = false)
abstract class AirTodayDatabase : RoomDatabase() {
    abstract fun airTodayDao(): AirTodayDao

    companion object {
        @Volatile
        private var INSTANCE: AirTodayDatabase? = null

        fun getDatabase(context: Context): AirTodayDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AirTodayDatabase::class.java,
                    "air_today_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
