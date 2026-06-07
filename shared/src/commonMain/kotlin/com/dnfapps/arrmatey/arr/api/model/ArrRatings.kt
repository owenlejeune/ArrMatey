package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.collections.contains
import kotlinx.serialization.Serializable

@Serializable(with = ArrRatingsSerializer::class)
sealed interface ArrRatings

object ArrRatingsSerializer : JsonContentPolymorphicSerializer<ArrRatings>(ArrRatings::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<ArrRatings> {
        val jsonObject = element.jsonObject

        return when {
            "votes" in jsonObject -> {
                if ("popularity" in jsonObject) BookshelfRatings.serializer()
                else if (jsonObject["value"]?.jsonPrimitive?.content?.contains(".") == true) LidarrRatings.serializer()
                else SeriesRatings.serializer()
            }
            "tmdb" in jsonObject -> MovieRatings.serializer()
            else -> throw SerializationException("Unknown MediaItem type")
        }
    }
}