package com.dnfapps.arrmatey.utils

import dev.icerock.moko.resources.desc.PluralStringDesc
import dev.icerock.moko.resources.desc.StringDesc

val fakeMokoStrings: MokoStrings = MokoStrings(
    resolver = object : StringResolver {
        override fun resolve(stringDesc: StringDesc): String = ""
        override fun resolve(pluralsDesc: PluralStringDesc): String = ""
    },
)
