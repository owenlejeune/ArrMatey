package com.dnfapps.arrmatey.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import org.koin.compose.currentKoinScope
import org.koin.compose.getKoin
import org.koin.core.parameter.parametersOf
import kotlin.reflect.KClass

@Composable
inline fun <reified T : Any> koinInjectParams(vararg params: Any?): T =
    if (ViewModel::class.java.isAssignableFrom(T::class.java)) {
        val key = remember(params) { "${T::class.qualifiedName}_${params.joinToString("_")}" }
        val scope = currentKoinScope()
        val factory = remember(key) {
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <VM : ViewModel> create(modelClass: Class<VM>, extras: CreationExtras): VM {
                    return scope.get(clazz = modelClass.kotlin) { parametersOf(*params) } as VM
                }

                @Suppress("UNCHECKED_CAST")
                override fun <VM : ViewModel> create(modelClass: Class<VM>): VM {
                    return scope.get(clazz = modelClass.kotlin) { parametersOf(*params) } as VM
                }
            }
        }
        @Suppress("UNCHECKED_CAST")
        viewModel(
            modelClass = (T::class as KClass<ViewModel>),
            key = key,
            factory = factory,
        ) as T
    } else {
        koinInjectWithParams(T::class, *params)
    }

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
