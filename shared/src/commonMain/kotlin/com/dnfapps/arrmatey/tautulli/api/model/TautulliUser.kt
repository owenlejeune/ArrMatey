package com.dnfapps.arrmatey.tautulli.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TautulliUser(
    @SerialName("user_id") val userId: Long,
    val username: String? = null,
    @SerialName("friendly_name") val friendlyName: String? = null,
    @SerialName("user_thumb") val userThumb: String? = null,
    val thumb: String? = null,
    val email: String? = null,
    @SerialName("is_active") val isActive: Int? = null,
    @SerialName("is_admin") val isAdmin: Int? = null,
    @SerialName("is_home_user") val isHomeUser: Int? = null,
    @SerialName("is_allow_sync") val isAllowSync: Int? = null,
    @SerialName("is_restricted") val isRestricted: Int? = null,
    @SerialName("do_notify") val doNotify: Int? = null,
    @SerialName("keep_history") val keepHistory: Int? = null,
    @SerialName("allow_guest") val allowGuest: Int? = null,
    @SerialName("shared_libraries") val sharedLibraries: List<String> = emptyList(),
) {
    val displayName: String
        get() = friendlyName?.takeIf { it.isNotBlank() } ?: username?.takeIf { it.isNotBlank() } ?: "User $userId"

    val avatarUrl: String?
        get() = userThumb ?: thumb
}
