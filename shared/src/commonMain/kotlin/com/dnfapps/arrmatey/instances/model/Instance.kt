package com.dnfapps.arrmatey.instances.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.AspectRatio
import com.dnfapps.arrmatey.utils.getNetworkUtils
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.StringResource

@Entity(
    tableName = "instances",
    indices = [
        Index(value = ["url"], unique = true),
        Index(value = ["label"], unique = true)
    ]
)
data class Instance(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: InstanceType,
    val label: String,
    val url: String,
    val apiKey: String,
    val basicAuthEnabled: Boolean = false,
    val enabled: Boolean = true,
    val slowInstance: Boolean = false,
    val customTimeout: Long? = null,
    val selected: Boolean = false,
    val notificationsEnabled: Boolean = false,
    val headers: List<InstanceHeader> = emptyList(),

    val localNetworkEnabled: Boolean = false,
    val localNetworkSsids: List<String> = emptyList(),
    val localNetworkEndpoint: String? = null
) {

    fun getEffectiveBaseUrl(): String {
        if (!localNetworkEnabled ||
            localNetworkSsids.isEmpty() ||
            localNetworkEndpoint.isNullOrBlank()
        ) {
            return url
        }
        return try {
            val currentSsid = getNetworkUtils().getCurrentWifiSsid()
            if (currentSsid != null && localNetworkSsids.any { it.equals(currentSsid, ignoreCase = true) }) {
                localNetworkEndpoint
            } else {
                url
            }
        } catch (e: Exception) {
            url
        }
    }

    fun isUsingLocalNetwork(): Boolean {
        return try {
            val currentSsid = getNetworkUtils().getCurrentWifiSsid()
            localNetworkEnabled &&
                    !localNetworkEndpoint.isNullOrBlank() &&
                    currentSsid != null &&
                    localNetworkSsids.any { it.equals(currentSsid, ignoreCase = true) }
        } catch (e: Exception) {
            false
        }
    }
}

enum class InstanceType(
    val resource: StringResource,
    val icon: ImageResource,
    val tabIcon: ImageResource?,
    val github: String,
    val website: String,
    val defaultPort: Int,
    val supportsActivityQueue: Boolean,
    val apiBase: String,
    val testEndpoint: String,
    val includeTopLevelAutomaticSearchOption: Boolean,
    val aspectRatio: AspectRatio,
    val supportsNotifications: Boolean
) {
    Sonarr(
        resource = MR.strings.sonarr_description,
        github = "https://github.com/Sonarr/Sonarr",
        website = "https://sonarr.tv/",
        icon = MR.images.sonarr,
        tabIcon = MR.images.sonarr_tab,
        defaultPort = 8989,
        supportsActivityQueue = true,
        apiBase = "api/v3",
        testEndpoint = "system/status",
        includeTopLevelAutomaticSearchOption = true,
        aspectRatio = AspectRatio.Poster,
        supportsNotifications = true
    ),
    Radarr(
        resource = MR.strings.radarr_description,
        github = "https://github.com/Radarr/Radarr",
        website = "https://radarr.video/",
        icon = MR.images.radarr,
        tabIcon = MR.images.radarr_tab,
        defaultPort = 7878,
        supportsActivityQueue = true,
        apiBase = "api/v3",
        testEndpoint = "system/status",
        includeTopLevelAutomaticSearchOption = false,
        aspectRatio = AspectRatio.Poster,
        supportsNotifications = true
    ),
    Lidarr(
        resource = MR.strings.lidarr_description,
        github = "https://github.com/Lidarr/Lidarr",
        website = "https://lidarr.audio/",
        icon = MR.images.lidarr,
        tabIcon = MR.images.lidarr_tab,
        defaultPort = 8686,
        supportsActivityQueue = true,
        apiBase = "api/v1",
        testEndpoint = "system/status",
        includeTopLevelAutomaticSearchOption = true,
        aspectRatio = AspectRatio.Cover,
        supportsNotifications = true
    ),
    Prowlarr(
        resource = MR.strings.prowlarr_description,
        github = "https://github.com/Prowlarr/Prowlarr",
        website = "https://prowlarr.com/",
        icon = MR.images.prowlarr,
        tabIcon = MR.images.prowlarr_tab,
        defaultPort = 9696,
        supportsActivityQueue = false,
        apiBase = "api/v1",
        testEndpoint = "system/status",
        includeTopLevelAutomaticSearchOption = false,
        aspectRatio = AspectRatio.Cover,
        supportsNotifications = false
    )
//    Seerr(
//        resource = MR.strings.seerr_description,
//        github = "https://github.com/seerr-team/seerr",
//        website = "https://docs.seerr.dev/",
//        iconKey = "seerr",
//        defaultPort = 5055,
//        supportsActivityQueue = false,
//        apiBase = "api/v1",
//        testEndpoint = "auth/me",
//        includeTopLevelAutomaticSearchOption = false,
//        aspectRatio = AspectRatio.Poster
//    )
}
