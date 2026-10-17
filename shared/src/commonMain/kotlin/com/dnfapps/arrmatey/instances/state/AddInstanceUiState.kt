package com.dnfapps.arrmatey.instances.state

import com.dnfapps.arrmatey.database.dao.ConflictField
import com.dnfapps.arrmatey.database.dao.InsertResult
import com.dnfapps.arrmatey.instances.model.HeaderRestrictionType
import com.dnfapps.arrmatey.instances.model.InstanceHeader
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.model.SeerrAuthType
import com.dnfapps.arrmatey.utils.isValidUrl

data class AddInstanceUiState(
    val apiEndpoint: String = "",
    val apiKey: String = "",
    val noApiKeyRequired: Boolean = false,
    val instanceLabel: String = "",
    val isSlowInstance: Boolean = false,
    val customTimeout: Long? = null,
    val endpointError: Boolean = false,
    val testing: Boolean = false,
    val localTesting: Boolean = false,
    val testResult: Boolean? = null,
    val localTestResult: Boolean? = null,
    val saveButtonEnabled: Boolean = false,
    val createResult: InsertResult? = null,
    val editResult: InsertResult? = null,
    val infoCardMaps: Map<InstanceType, Boolean> = emptyMap(),
    val headers: List<InstanceHeader> = emptyList(),
    val localNetworkEnabled: Boolean = false,
    val localNetworkUrl: String = "",
    val localNetworkSsids: List<String> = emptyList(),
    val localNetworkUrlError: Boolean = false,
    val notificationsEnabled: Boolean = false,
    val seerrAuthType: SeerrAuthType = SeerrAuthType.ApiKey,
    val sessionToken: String = "",
    val authenticatedUserDisplayName: String? = null,
    val isAuthenticating: Boolean = false,
    val authError: String? = null,
    val quickConnectCode: String? = null,
    val quickConnectSecret: String? = null,
    val plexToken: String = "",
    val plexAuthUrl: String? = null,
    val isPlexWaiting: Boolean = false,
) {
    constructor() : this("") // helper for iOS

    val localNetworkConfigured: Boolean
        get() = localNetworkEnabled && localNetworkUrl.isNotEmpty() && localNetworkSsids.isNotEmpty()

    val hasLabelConflict: Boolean
        get() = (createResult as? InsertResult.Conflict ?: editResult as? InsertResult.Conflict)
            ?.fields
            ?.contains(ConflictField.InstanceLabel) == true

    val hasUrlConflict: Boolean
        get() = (createResult as? InsertResult.Conflict ?: editResult as? InsertResult.Conflict)
            ?.fields
            ?.contains(ConflictField.InstanceUrl) == true

    fun shouldShowInfoCard(type: InstanceType): Boolean = infoCardMaps[type] ?: false

    fun isTestButtonEnabled(type: InstanceType): Boolean {
        if (testing || apiEndpoint.isEmpty()) return false
        if (type == InstanceType.Seerr && seerrAuthType != SeerrAuthType.ApiKey) {
            return sessionToken.isNotEmpty()
        }
        return noApiKeyRequired || apiKey.isNotEmpty()
    }

    fun validate(): AddInstanceUiState {
        val authValid =
            if (seerrAuthType != SeerrAuthType.ApiKey) {
                sessionToken.isNotEmpty()
            } else {
                noApiKeyRequired || apiKey.isNotEmpty()
            }
        val isValid =
            apiEndpoint.isNotEmpty() &&
                authValid &&
                instanceLabel.isNotEmpty() &&
                (!localNetworkEnabled || (localNetworkUrl.isValidUrl() && localNetworkSsids.isNotEmpty())) &&
                headers.all { it.restrictionType != HeaderRestrictionType.SpecificSsids || it.restrictedSsids.isNotEmpty() }
        return copy(saveButtonEnabled = isValid)
    }
}
