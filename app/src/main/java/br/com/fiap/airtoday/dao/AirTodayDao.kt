package br.com.fiap.airtoday.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.fiap.airtoday.model.AirToday

@Dao
interface AirTodayDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAirQuality(data: AirToday)

    @Query("SELECT * FROM air_today ORDER BY timestamp DESC")
    suspend fun getAllAirQuality(): List<AirToday>

    @Query("DELETE FROM air_today")
    suspend fun clearAirQualityData()
}
