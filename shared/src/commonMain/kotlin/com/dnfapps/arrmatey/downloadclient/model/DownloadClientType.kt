package com.dnfapps.arrmatey.downloadclient.model

import com.dnfapps.arrmatey.shared.MR
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.StringResource

enum class DownloadClientType(
    val displayName: String,
    val description: StringResource,
    val defaultPort: Int,
    val icon: ImageResource,
    val tabIcon: ImageResource,
    val github: String,
    val website: String
) {
    QBittorrent(
        displayName = "qBittorrent",
        description = MR.strings.qbitt_description,
        defaultPort = 8080,
        icon = MR.images.qbittorrent,
        tabIcon = MR.images.qbittorrent_tab,
        github = "https://github.com/qbittorrent/qBittorrent",
        website = "https://www.qbittorrent.org/"
    ),
    SABnzbd(
        displayName = "SABnzbd",
        description = MR.strings.sabnzbd_description,
        defaultPort = 8080,
        icon = MR.images.sabnzbd,
        tabIcon = MR.images.sabnzbd_tab,
        github = "https://github.com/sabnzbd/sabnzbd",
        website = "https://sabnzbd.org/"
    ),
    Deluge(
        displayName = "Deluge",
        description = MR.strings.deluge_description,
        defaultPort = 8112,
        icon = MR.images.deluge,
        tabIcon = MR.images.deluge_tab,
        github = "https://github.com/deluge-torrent/deluge",
        website = "https://www.deluge-torrent.org/"
    ),
    Transmission(
        displayName = "Transmission",
        description = MR.strings.transmission_description,
        defaultPort = 9091,
        icon = MR.images.transmission,
        tabIcon = MR.images.transmission_tab,
        github = "https://github.com/transmission/transmission",
        website = "https://transmissionbt.com/"
    )
}
