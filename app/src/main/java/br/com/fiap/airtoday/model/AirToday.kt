package br.com.fiap.airtoday.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "air_today")
data class AirToday(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val city: String,
    val aqi: Int,
    val temperature: Double?,
    val humidity: Int?,
    val timestamp: Long
)
