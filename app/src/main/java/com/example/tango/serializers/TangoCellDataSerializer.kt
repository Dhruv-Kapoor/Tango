package com.example.tango.serializers

import com.example.tango.dataClasses.TangoCellData
import com.example.tango.dataClasses.TangoCellValue
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

class TangoCellDataSerializer: JsonSerializer<TangoCellData>, JsonDeserializer<TangoCellData> {
    override fun serialize(
        src: TangoCellData?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement? {
        return JsonObject().apply {
            addProperty("value", src?.value)
            addProperty("disabled", src?.disabled)
            add("leftSymbol", context?.serialize(src?.leftSymbol))
            add("topSymbol", context?.serialize(src?.topSymbol))
        }
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): TangoCellData? {
        val jsonObject = json?.asJsonObject
        if (jsonObject != null) {
            return TangoCellData(
                value = jsonObject.get("value")!!.asInt,
                disabled = jsonObject.get("disabled")!!.asBoolean,
                leftSymbol = jsonObject.get("leftSymbol")!!.asInt,
                topSymbol = jsonObject.get("topSymbol")!!.asInt,
            )
        }
        return null
    }
}