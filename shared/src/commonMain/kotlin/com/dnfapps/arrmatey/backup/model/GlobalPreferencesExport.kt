package com.dnfapps.arrmatey.backup.model

import com.dnfapps.arrmatey.datastore.TabPreferences
import kotlinx.serialization.Serializable

@Serializable
data class GlobalPreferencesExport(
    val tabPreferences: TabPreferences? = null,
    val useServiceNavLogos: Boolean? = null,
    val hideInstanceSwitcher: Boolean? = null
)
