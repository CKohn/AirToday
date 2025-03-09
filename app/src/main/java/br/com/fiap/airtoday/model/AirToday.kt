package br.com.fiap.airtoday.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "air_today")
data class AirToday(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val city: String,
    val aqi: Int, // Índice de qualidade do ar
    val temperature: Double?,
    val humidity: Int?,
    val pm25: Double?,
    val pm10: Double?,
    val o3: Double?,
    val no2: Double?,
    val so2: Double?,
    val co: Double?,
    val timestamp: Long // Data e hora do registro
)
