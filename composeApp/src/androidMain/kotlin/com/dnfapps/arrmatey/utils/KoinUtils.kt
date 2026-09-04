package com.dnfapps.arrmatey.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.koin.compose.getKoin
import org.koin.core.parameter.parametersOf
import kotlin.reflect.KClass

@Composable
inline fun <reified T : Any> koinInjectParams(vararg params: Any?): T = koinInjectWithParams(T::class, *params)

@Composable
fun <T : Any> koinInjectWithParams(
    clazz: KClass<T>,
    vararg params: Any?,
): T {
    val koin = getKoin()
    val keys = remember(params) { params.asList() }
    return remember(clazz, keys) {
        koin.get(clazz = clazz) { parametersOf(*params) }
    }
}
