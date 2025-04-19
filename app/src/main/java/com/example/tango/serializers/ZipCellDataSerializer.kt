package com.example.tango.serializers

import com.example.tango.dataClasses.ZipCellData
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type


class ZipCellDataSerializer : JsonSerializer<ZipCellData>, JsonDeserializer<ZipCellData> {
    override fun serialize(
        src: ZipCellData?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement? {
        return JsonObject().apply {
            addProperty("value", src?.value)
            addProperty("topWall", src?.topWall)
            addProperty("leftWall", src?.leftWall)
            addProperty("pathPosition", src?.pathPosition)
        }
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ZipCellData? {
        val jsonObject = json?.asJsonObject
        if (jsonObject != null) {
            return ZipCellData(
                value = if (jsonObject.get("value") == null || jsonObject.get("value").isJsonNull) null else jsonObject.get(
                    "value"
                ).asInt,
                topWall = jsonObject.get("topWall")!!.asBoolean,
                leftWall = jsonObject.get("leftWall")!!.asBoolean,
                pathPosition = if (jsonObject.get("pathPosition") == null || jsonObject.get("pathPosition").isJsonNull) null else jsonObject.get(
                    "pathPosition"
                ).asInt
            )
        }
        return null
    }

}