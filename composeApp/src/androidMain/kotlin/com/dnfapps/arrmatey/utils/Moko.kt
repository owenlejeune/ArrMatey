package com.dnfapps.arrmatey.utils

import androidx.compose.runtime.Composable
import dev.icerock.moko.resources.PluralsResource
import dev.icerock.moko.resources.StringResource
import org.koin.compose.koinInject

@Composable
fun mokoString(resource: StringResource): String {
    val moko: MokoStrings = koinInject()
    return moko.getString(resource)
}

@Composable
fun mokoString(
    resource: StringResource,
    vararg formatArgs: Any,
): String {
    val moko: MokoStrings = koinInject()
    return moko.getString(resource, formatArgs.toList())
}

@Composable
fun mokoPlural(
    resource: PluralsResource,
    quantity: Int,
): String {
    val moko: MokoStrings = koinInject()
    return moko.getPlural(resource, quantity, listOf(quantity))
}

@Composable
fun mokoPlural(
    resource: PluralsResource,
    quantity: Int,
    vararg formatArgs: Any,
): String {
    val moko: MokoStrings = koinInject()
    return moko.getPlural(resource, quantity, formatArgs.toList())
}
