package com.dnfapps.arrmatey.utils

import android.content.Context
import dev.icerock.moko.resources.desc.PluralStringDesc
import dev.icerock.moko.resources.desc.StringDesc
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

actual fun createResolver(): StringResolver =
    object : KoinComponent, StringResolver {
        private val context: Context = get()

        override fun resolve(stringDesc: StringDesc): String = stringDesc.toString(context)

        override fun resolve(pluralsDesc: PluralStringDesc): String = pluralsDesc.toString(context)
    }
