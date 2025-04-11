package com.example.tango.serializers

import com.example.tango.dataClasses.QueensCellData
import com.example.tango.dataClasses.TangoCellData
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

class QueensCellDataSerializer : JsonSerializer<QueensCellData>, JsonDeserializer<QueensCellData> {
    override fun serialize(
        src: QueensCellData?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement? {
        return JsonObject().apply {
            addProperty("value", src?.value)
            addProperty("color", src?.color)
        }
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): QueensCellData? {
        val jsonObject = json?.asJsonObject
        if (jsonObject != null) {
            return QueensCellData(
                value = jsonObject.get("value")!!.asInt,
                color = jsonObject.get("color")!!.asInt,
            )
        }
        return null
    }

}