package com.dnfapps.arrmatey.datastore

import com.dnfapps.arrmatey.compose.TabItem
import kotlinx.serialization.Serializable

@Serializable
data class TabPreferences(
    val orderedVisibleKeys: List<String> = TabItem.defaultStandardKeys(),
    val orderedHiddenKeys: List<String> = TabItem.defaultHiddenKeys()
) {
    constructor(): this(TabItem.defaultStandardKeys()) // empty ios constructor
}