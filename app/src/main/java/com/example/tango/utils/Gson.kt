package com.example.tango.utils

import com.example.tango.dataClasses.QueensCellData
import com.example.tango.dataClasses.TangoCellData
import com.example.tango.dataClasses.ZipCellData
import com.example.tango.serializers.QueensCellDataSerializer
import com.example.tango.serializers.TangoCellDataSerializer
import com.example.tango.serializers.ZipCellDataSerializer
import com.google.gson.Gson
import com.google.gson.GsonBuilder

object Gson {
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(TangoCellData::class.java, TangoCellDataSerializer())
        .registerTypeAdapter(QueensCellData::class.java, QueensCellDataSerializer())
        .registerTypeAdapter(ZipCellData::class.java, ZipCellDataSerializer())
        .create()

    fun getGson(): Gson {
        return gson
    }
}
