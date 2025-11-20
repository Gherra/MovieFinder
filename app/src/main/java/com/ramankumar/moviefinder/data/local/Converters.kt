package com.ramankumar.moviefinder.data.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromIntList(list: List<Int>?): String? {
        return list?.joinToString(",")
    }

    @TypeConverter
    fun toIntList(csv: String?): List<Int>? {
        return csv?.takeIf { it.isNotBlank() }?.split(",")?.map { it.toInt() }
    }
}