package com.kkh.wallet.data.local.converter

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun stringListToString(value: List<String>?): String =
        value?.joinToString("\u001F") ?: ""

    @TypeConverter
    fun stringToStringList(value: String?): List<String> =
        if (value.isNullOrEmpty()) emptyList() else value.split("\u001F")
}
