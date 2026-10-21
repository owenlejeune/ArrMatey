package com.dnfapps.arrmatey.downloadclient.model

import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.StringResource
import kotlinx.serialization.Serializable

@Serializable
enum class DownloadClientType(
    val displayName: String,
    val description: StringResource,
    val defaultPort: Int,
    val icon: ImageResource,
    val tabIcon: ImageResource,
    val github: String,
    val website: String,
    val supportsExternalIp: Boolean = false,
    val supportsUpload: Boolean = true,
) {
    SABnzbd(
        displayName = "SABnzbd",
        description = MR.strings.sabnzbd_description,
        defaultPort = 8080,
        icon = MR.images.sabnzbd,
        tabIcon = MR.images.sabnzbd_tab,
        github = "https://github.com/sabnzbd/sabnzbd",
        website = "https://sabnzbd.org/",
        supportsUpload = false,
    ),
    NZBGet(
        displayName = "NZBGet",
        description = MR.strings.nzbget_description,
        defaultPort = 6789,
        icon = MR.images.nzbget,
        tabIcon = MR.images.nzbget_tab,
        github = "https://github.com/nzbget-ng/nzbget",
        website = "https://nzbget.com/",
        supportsUpload = false,
    ),
    QBittorrent(
        displayName = "qBittorrent",
        description = MR.strings.qbitt_description,
        defaultPort = 8080,
        icon = MR.images.qbittorrent,
        tabIcon = MR.images.qbittorrent_tab,
        github = "https://github.com/qbittorrent/qBittorrent",
        website = "https://www.qbittorrent.org/",
        supportsExternalIp = true,
        supportsUpload = true,
    ),
    Deluge(
        displayName = "Deluge",
        description = MR.strings.deluge_description,
        defaultPort = 8112,
        icon = MR.images.deluge,
        tabIcon = MR.images.deluge_tab,
        github = "https://github.com/deluge-torrent/deluge",
        website = "https://www.deluge-torrent.org/",
        supportsExternalIp = true,
        supportsUpload = true,
    ),
    Transmission(
        displayName = "Transmission",
        description = MR.strings.transmission_description,
        defaultPort = 9091,
        icon = MR.images.transmission,
        tabIcon = MR.images.transmission_tab,
        github = "https://github.com/transmission/transmission",
        website = "https://transmissionbt.com/",
        supportsUpload = true,
    ),
    RTorrent(
        displayName = "rTorrent",
        description = MR.strings.rtorrent_description,
        defaultPort = 8000,
        icon = MR.images.rtorrent,
        tabIcon = MR.images.rtorrent_tab,
        github = "https://github.com/rakshasa/rtorrent",
        website = "https://rakshasa.github.io/rtorrent/",
    ),
    ;

    val isUsenet: Boolean
        get() = !supportsUpload

    val isTorrent: Boolean
        get() = supportsUpload
}
