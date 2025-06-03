package com.example.tango.serializers

import com.example.tango.dataClasses.TicTacToeCellData
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

class TicTacToeCellDataSerializer : JsonSerializer<TicTacToeCellData>, JsonDeserializer<TicTacToeCellData>  {
    override fun serialize(
        src: TicTacToeCellData?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement? {
        return JsonObject().apply {
            addProperty("value", src?.value)
            addProperty("partial", src?.partial)
        }
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): TicTacToeCellData? {
        val jsonObject = json?.asJsonObject
        if (jsonObject != null) {
            return TicTacToeCellData(
                value =  jsonObject.get("value")!!.asInt,
                partial =  jsonObject.get("partial")!!.asBoolean
            )
        }
        return null
    }

}