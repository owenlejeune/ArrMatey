package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.api.model.CustomFilterItem
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class ApplyCustomFilterItemUseCase {
    operator fun invoke(itemValue: Any?, filter: CustomFilterItem): Boolean {
        val filterValue = filter.value
        return when (filter.type) {
            "equal", "is" -> compareEqual(itemValue, filterValue)
            "notEqual", "isNot" -> !compareEqual(itemValue, filterValue)
            "contains" -> compareContains(itemValue, filterValue)
            "doesNotContain" -> !compareContains(itemValue, filterValue)
            "startsWith" -> (itemValue as? String)?.startsWith(filterValue.asString(), ignoreCase = true) == true
            "doesNotStartWith" -> (itemValue as? String)?.startsWith(filterValue.asString(), ignoreCase = true) == false
            "endsWith" -> (itemValue as? String)?.endsWith(filterValue.asString(), ignoreCase = true) == true
            "doesNotEndWith" -> (itemValue as? String)?.endsWith(filterValue.asString(), ignoreCase = true) == false
            "greaterThan" -> compareNumber(itemValue, filterValue) { it > 0 }
            "greaterThanOrEqual" -> compareNumber(itemValue, filterValue) { it >= 0 }
            "lessThan" -> compareNumber(itemValue, filterValue) { it < 0 }
            "lessThanOrEqual" -> compareNumber(itemValue, filterValue) { it <= 0 }
            "inLast", "notInLast", "inNext", "notInNext" -> compareDate(itemValue, filterValue, filter.type)
            else -> true
        }
    }

    private fun compareEqual(itemValue: Any?, filterValue: JsonElement): Boolean {
        if (itemValue == null) return filterValue is JsonPrimitive && filterValue.content == "null"
        
        return when (filterValue) {
            is JsonPrimitive -> {
                val bool = filterValue.booleanOrNull
                if (bool != null) {
                    if (itemValue is Boolean) {
                        itemValue == bool
                    } else {
                        itemValue.toString().equals(bool.toString(), ignoreCase = true)
                    }
                } else {
                    val num = filterValue.doubleOrNull
                    if (num != null) {
                        (itemValue as? Number)?.toDouble() == num
                    } else {
                        itemValue.toString().equals(filterValue.content, ignoreCase = true)
                    }
                }
            }
            is JsonArray -> {
                val list = filterValue.map { it.asString().lowercase() }
                if (itemValue is List<*>) {
                    itemValue.any { it.toString().lowercase() in list }
                } else {
                    itemValue.toString().lowercase() in list
                }
            }
            else -> false
        }
    }

    private fun compareContains(itemValue: Any?, filterValue: JsonElement): Boolean {
        if (itemValue == null) return false
        
        val filterValues: List<String> = when (filterValue) {
            is JsonArray -> filterValue.map { it.asString().lowercase() }
            else -> listOf(filterValue.asString().lowercase())
        }
        
        val itemStrings: List<String> = when (itemValue) {
            is List<*> -> itemValue.map { it.toString().lowercase() }
            else -> listOf(itemValue.toString().lowercase())
        }
        
        return filterValues.any { filterStr ->
            itemStrings.any { itemStr -> itemStr.contains(filterStr) }
        }
    }

    private fun compareNumber(itemValue: Any?, filterValue: JsonElement, predicate: (Double) -> Boolean): Boolean {
        val itemNum = (itemValue as? Number)?.toDouble() ?: return false
        val filterNum = filterValue.asDouble() ?: return false
        return predicate(itemNum - filterNum)
    }

    private fun compareDate(itemValue: Any?, filterValue: JsonElement, filterType: String): Boolean {
        val itemInstant: Instant = when (itemValue) {
            is Long -> Instant.fromEpochMilliseconds(itemValue)
            is Instant -> itemValue
            else -> return false
        }

        val now = Clock.System.now()

        val filterObj = filterValue as? JsonObject ?: return false
        val value = filterObj["value"]?.jsonPrimitive?.doubleOrNull ?: return false
        val timeUnit = filterObj["time"]?.jsonPrimitive?.content ?: "days"

        val duration: Duration = when (timeUnit) {
            "seconds" -> value.seconds
            "minutes" -> value.minutes
            "hours" -> value.hours
            "days" -> value.days
            "weeks" -> (value * 7).days
            "months" -> (value * 30).days // Approximate
            else -> value.days
        }

        return when (filterType) {
            "inLast" -> itemInstant >= now.minus(duration) && itemInstant <= now
            "notInLast" -> itemInstant < now.minus(duration) || itemInstant > now
            "inNext" -> itemInstant >= now && itemInstant <= now.plus(duration)
            "notInNext" -> itemInstant < now || itemInstant > now.plus(duration)
            else -> true
        }
    }

    private fun JsonElement.asString(): String = (this as? JsonPrimitive)?.content ?: ""
    private fun JsonElement.asDouble(): Double? = (this as? JsonPrimitive)?.doubleOrNull
}
