package com.dnfapps.arrmatey.utils

import androidx.compose.runtime.Composable
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@Composable
inline fun <reified T> koinInjectParams(vararg params: Any?): T = koinInject { parametersOf(*params) }
