package com.example.tango.utils

import com.example.tango.dataClasses.TangoCellData
import com.example.tango.serializers.TangoCellDataSerializer
import com.google.gson.Gson
import com.google.gson.GsonBuilder

object Gson {
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(TangoCellData::class.java, TangoCellDataSerializer())
        .create()

    fun getGson(): Gson {
        return gson
    }
}
