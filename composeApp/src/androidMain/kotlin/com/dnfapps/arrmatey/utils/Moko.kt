package com.dnfapps.arrmatey.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import dev.icerock.moko.resources.PluralsResource
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.Plural
import dev.icerock.moko.resources.desc.PluralFormatted
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
import org.koin.compose.koinInject

@Composable
fun mokoString(resource: StringResource): String {
    if (LocalInspectionMode.current) {
        return StringDesc.Resource(resource).toString(LocalContext.current)
    }
    val moko: MokoStrings = koinInject()
    return moko.getString(resource)
}

@Composable
fun mokoString(
    resource: StringResource,
    vararg formatArgs: Any,
): String {
    if (LocalInspectionMode.current) {
        return StringDesc.ResourceFormatted(resource, formatArgs.toList()).toString(LocalContext.current)
    }
    val moko: MokoStrings = koinInject()
    return moko.getString(resource, formatArgs.toList())
}

@Composable
fun mokoPlural(
    resource: PluralsResource,
    quantity: Int,
): String {
    if (LocalInspectionMode.current) {
        return StringDesc.Plural(resource, quantity).toString(LocalContext.current)
    }
    val moko: MokoStrings = koinInject()
    return moko.getPlural(resource, quantity, listOf(quantity))
}

@Composable
fun mokoPlural(
    resource: PluralsResource,
    quantity: Long,
): String {
    if (LocalInspectionMode.current) {
        return StringDesc.Plural(resource, quantity.toInt()).toString(LocalContext.current)
    }
    val moko: MokoStrings = koinInject()
    return moko.getPlural(resource, quantity.toInt(), listOf(quantity.toInt()))
}

@Composable
fun mokoPlural(
    resource: PluralsResource,
    quantity: Int,
    vararg formatArgs: Any,
): String {
    if (LocalInspectionMode.current) {
        return StringDesc.PluralFormatted(resource, quantity, formatArgs.toList()).toString(LocalContext.current)
    }
    val moko: MokoStrings = koinInject()
    return moko.getPlural(resource, quantity, formatArgs.toList())
}
