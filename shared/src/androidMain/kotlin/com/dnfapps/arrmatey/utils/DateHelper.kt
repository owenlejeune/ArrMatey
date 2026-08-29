package com.dnfapps.arrmatey.utils

import android.content.Context
import android.text.format.DateFormat
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.getValue

object DateHelper : KoinComponent {
    private val context: Context by inject()

    fun is24Hour() = DateFormat.is24HourFormat(context)
}
