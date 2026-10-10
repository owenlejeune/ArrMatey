package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KapowarrAboutResponse(
    val error: String? = null,
    val result: KapowarrSystemInfo? = null,
)

@Serializable
data class KapowarrSystemInfo(
    val version: String? = null,
    @SerialName("python_version") val pythonVersion: String? = null,
    @SerialName("database_version") val databaseVersion: Int? = null,
    @SerialName("database_location") val databaseLocation: String? = null,
    @SerialName("data_folder") val dataFolder: String? = null,
    val os: String? = null,
    @SerialName("runs_64bit") val runs64bit: Boolean = false,
) {
    fun toArrSoftwareStatus(): ArrSoftwareStatus = ArrSoftwareStatus(
        appName = "Kapowarr",
        version = version,
        startupPath = databaseLocation,
        appData = dataFolder,
        osName = os,
        runtimeName = pythonVersion?.let { "Python $it" },
        databaseVersion = databaseVersion?.toString(),
    )
}
