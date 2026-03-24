package com.dnfapps.arrmatey.entensions

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.ManageSearch
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.ui.graphics.vector.ImageVector
import com.dnfapps.arrmatey.arr.state.ContentFilter
import com.dnfapps.arrmatey.compose.TabItem
import com.dnfapps.arrmatey.compose.utils.SortBy
import com.dnfapps.arrmatey.compose.utils.SortOrder
import com.dnfapps.arrmatey.ui.icons.Hard_drive
import com.dnfapps.arrmatey.ui.theme.ViewType

val ContentFilter.imageVector: ImageVector
    get() = when(this) {
        ContentFilter.All -> Icons.Default.VideoLibrary
        ContentFilter.MoviesOnly -> Icons.Default.Movie
        ContentFilter.EpisodesOnly -> Icons.Default.Tv
        ContentFilter.AlbumsOnly -> Icons.Default.MusicNote
    }

val TabItem.androidIcon: ImageVector
    get() = when(this) {
        is TabItem.Standard -> when (this) {
            TabItem.Standard.SHOWS -> Icons.Default.Tv
            TabItem.Standard.MOVIES -> Icons.Default.Movie
            TabItem.Standard.MUSIC -> Icons.Default.MusicNote
            TabItem.Standard.ACTIVITY -> Icons.Default.Download
            TabItem.Standard.DOWNLOADS -> Icons.Default.CloudDownload
            TabItem.Standard.CALENDAR -> Icons.Default.CalendarMonth
            TabItem.Standard.REQUESTS -> Icons.Default.Inbox
            TabItem.Standard.PROWLARR -> Icons.Default.ManageSearch
        }
        is TabItem.CustomWebpage -> Icons.Default.Language
        is TabItem.Settings -> Icons.Default.Settings
    }

val SortBy.androidIcon: ImageVector
    get() = when(this) {
        SortBy.Title -> Icons.Default.SortByAlpha
        SortBy.Year -> Icons.Default.CalendarMonth
        SortBy.Added -> Icons.Default.Schedule
        SortBy.Rating -> Icons.Default.Star
        SortBy.FileSize -> Hard_drive
        SortBy.Grabbed -> Icons.Default.ArrowCircleDown
        SortBy.DigitalRelease -> Icons.Default.Tv
        SortBy.NextAiring -> Icons.Default.Schedule
        SortBy.PreviousAiring -> Icons.Default.History
        SortBy.Relevance -> Icons.Default.Star
        SortBy.Size -> Hard_drive
        SortBy.Progress -> Icons.Default.Downloading
        SortBy.DownloadSpeed -> Icons.Default.FileDownload
        SortBy.UploadSpeed -> Icons.Default.FileUpload
        SortBy.Eta -> Icons.Default.Schedule
        SortBy.Name -> Icons.Default.SortByAlpha
        SortBy.Priority -> Icons.Default.Star
        SortBy.Protocol -> Icons.Default.Download
        SortBy.Privacy -> Icons.Default.Visibility
    }

val SortOrder.androidIcon: ImageVector
    get() = when(this) {
        SortOrder.Asc -> Icons.Default.ArrowUpward
        SortOrder.Desc -> Icons.Default.ArrowDownward
    }

val ViewType.imageVector: ImageVector
    get() = when(this) {
        ViewType.Grid -> Icons.Default.GridView
        ViewType.List -> Icons.AutoMirrored.Default.List
    }