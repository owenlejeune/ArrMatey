package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.shared.MR
import dev.icerock.moko.resources.StringResource
import kotlinx.serialization.Serializable

@Serializable
data class ArrSoftwareStatus(
    val appName: String? = null,
    val instanceName: String? = null,
    val version: String? = null,
    val buildTime: String? = null,
    val isDebug: Boolean = false,
    val isProduction: Boolean = false,
    val isAdmin: Boolean = false,
    val isUserInteractive: Boolean = false,
    val startupPath: String? = null,
    val appData: String? = null,
    val osName: String? = null,
    val osVersion: String? = null,
    val isNetCore: Boolean = false,
    val isLinux: Boolean = false,
    val isOsx: Boolean = false,
    val isWindows: Boolean = false,
    val isDocker: Boolean = false,
    val mode: AppMode? = null,
    val branch: String? = null,
    val authentication: AuthType? = null,
    val sqliteVersion: String? = null,
    val migrationVersion: Int = 0,
    val urlBase: String? = null,
    val runtimeVersion: String? = null,
    val runtimeName: String? = null,
    val startTime: String? = null,
    val packageVersion: String? = null,
    val packageAuthor: String? = null,
    val packageUpdateMechanism: UpdateMechanism? = null,
    val packageUpdateMechanismMessage: String? = null,
    val databaseVersion: String? = null,
    val databaseType: DatabaseType? = null,
) {
    val hostPlatform: StringResource
        get() =
            when {
                isDocker -> MR.strings.docker
                isLinux -> MR.strings.linux
                isOsx -> MR.strings.macos
                isWindows -> MR.strings.windows
                else -> MR.strings.unknown
            }

    val hostOs: String?
        get() =
            buildString {
                osName?.let { os ->
                    append(os)
                    osVersion?.let { v ->
                        append(" ($v)")
                    }
                }
            }.takeUnless { it.isEmpty() }
}
