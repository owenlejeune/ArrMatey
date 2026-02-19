package com.dnfapps.arrmatey.instances.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.AspectRatio
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
    val enabled: Boolean = true,
    val slowInstance: Boolean = false,
    val customTimeout: Long? = null,
    val selected: Boolean = false,
    val headers: List<InstanceHeader> = emptyList()
)

enum class InstanceType(
    val resource: StringResource,
    val iconKey: String,
    val github: String,
    val website: String,
    val defaultPort: Int,
    val supportsActivityQueue: Boolean,
    val apiBase: String,
    val includeTopLevelAutomaticSearchOption: Boolean,
    val aspectRatio: AspectRatio
) {
    Sonarr(
        resource = MR.strings.sonarr_description,
        github = "https://github.com/Sonarr/Sonarr",
        website = "https://sonarr.tv/",
        iconKey = "sonarr",
        defaultPort = 8989,
        supportsActivityQueue = true,
        apiBase = "api/v3",
        includeTopLevelAutomaticSearchOption = true,
        aspectRatio = AspectRatio.Poster
    ),
    Radarr(
        resource = MR.strings.radarr_description,
        github = "https://github.com/Radarr/Radarr",
        website = "https://radarr.video/",
        iconKey = "radarr",
        defaultPort = 7878,
        supportsActivityQueue = true,
        apiBase = "api/v3",
        includeTopLevelAutomaticSearchOption = false,
        aspectRatio = AspectRatio.Poster
    ),
    Lidarr(
        resource = MR.strings.lidarr_description,
        github = "https://github.com/Lidarr/Lidarr",
        website = "https://lidarr.audio/",
        iconKey = "lidarr",
        defaultPort = 8686,
        supportsActivityQueue = true,
        apiBase = "api/v1",
        includeTopLevelAutomaticSearchOption = true,
        aspectRatio = AspectRatio.Cover
    ),
    Prowlarr(
        resource = MR.strings.prowlarr_description,
        github = "https://github.com/Prowlarr/Prowlarr",
        website = "https://prowlarr.com/",
        iconKey = "prowlarr",
        defaultPort = 9696,
        supportsActivityQueue = false,
        apiBase = "api/v1",
        includeTopLevelAutomaticSearchOption = false,
        aspectRatio = AspectRatio.Cover
    )
}
