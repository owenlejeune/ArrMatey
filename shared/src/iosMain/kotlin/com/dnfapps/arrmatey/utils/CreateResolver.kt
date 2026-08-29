package com.dnfapps.arrmatey.utils

import dev.icerock.moko.resources.desc.PluralStringDesc
import dev.icerock.moko.resources.desc.StringDesc

actual fun createResolver(): StringResolver =
    object : StringResolver {
        override fun resolve(stringDesc: StringDesc): String = stringDesc.localized()

        override fun resolve(pluralsDesc: PluralStringDesc): String = pluralsDesc.localized()
    }
