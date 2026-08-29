package com.dnfapps.arrmatey.utils

import dev.icerock.moko.resources.PluralsResource
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.Plural
import dev.icerock.moko.resources.desc.PluralFormatted
import dev.icerock.moko.resources.desc.PluralStringDesc
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc

interface StringResolver {
    fun resolve(stringDesc: StringDesc): String

    fun resolve(pluralsDesc: PluralStringDesc): String
}

class MokoStrings {
    private val resolver: StringResolver = createResolver()

    fun getString(resource: StringResource): String {
        val desc = StringDesc.Resource(resource)
        return resolver.resolve(desc)
    }

    fun getString(
        resource: StringResource,
        formatArgs: List<Any>,
    ): String {
        val desc = StringDesc.ResourceFormatted(resource, formatArgs)
        return resolver.resolve(desc)
    }

    fun getPlural(
        resource: PluralsResource,
        quantity: Int,
    ): String {
        val desc = StringDesc.Plural(resource, quantity)
        return resolver.resolve(desc)
    }

    fun getPlural(
        resource: PluralsResource,
        quantity: Int,
        formatArgs: List<Any>,
    ): String {
        val desc = StringDesc.PluralFormatted(resource, quantity, formatArgs)
        return resolver.resolve(desc)
    }
}
