package com.dnfapps.arrmatey.arr.api.client

import com.dnfapps.arrmatey.arr.api.model.AlbumMonitorBody
import com.dnfapps.arrmatey.arr.api.model.ApplyTags
import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.Arrtist
import com.dnfapps.arrmatey.arr.api.model.ArtistEditorBody
import com.dnfapps.arrmatey.arr.api.model.CommandPayload
import com.dnfapps.arrmatey.arr.api.model.CommandResponse
import com.dnfapps.arrmatey.arr.api.model.DeleteTrackBody
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.arr.api.model.HistoryItem
import com.dnfapps.arrmatey.arr.api.model.LidarrHistoryResponse
import com.dnfapps.arrmatey.arr.api.model.LidarrRelease
import com.dnfapps.arrmatey.arr.api.model.LidarrTrack
import com.dnfapps.arrmatey.arr.api.model.LidarrTrackFile
import com.dnfapps.arrmatey.arr.api.model.MonitoredResponse
import com.dnfapps.arrmatey.arr.api.model.ReleaseParams
import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.client.mapValues
import com.dnfapps.arrmatey.instances.model.Instance
import io.ktor.client.HttpClient
import kotlinx.datetime.LocalDate

class LidarrClient(
    override val instance: Instance,
    httpClient: HttpClient
): BaseArrClient(httpClient), ArrClient {

    override suspend fun getLibrary(): NetworkResult<List<Arrtist>> =
        get<List<Arrtist>>("artist")

    override suspend fun getDetail(id: Long): NetworkResult<Arrtist> =
        get<Arrtist>("artist/$id")

    override suspend fun update(item: ArrMedia): NetworkResult<Arrtist> =
        put<ArrMedia, Arrtist>("artist/${item.id}", item)

    override suspend fun edit(
        item: ArrMedia,
        moveFiles: Boolean
    ): NetworkResult<Unit> {
        val artist = item as? Arrtist
            ?: return NetworkResult.Error(message = "Item must be an Arrtist")
        val id = artist.id
            ?: return NetworkResult.Error(message = "Item id cannot be null")
        val body = ArtistEditorBody(
            artistIds = listOf(id),
            monitored = artist.monitored,
            monitorNewItems = artist.monitorNewItems,
            qualityProfileId = artist.qualityProfileId,
            rootFolderPath = artist.rootFolderPath,
            tags = artist.tags,
            applyTags = ApplyTags.Replace,
            moveFiles = moveFiles
        )
        return put("artist/editor", body = body)
    }

    override suspend fun delete(
        id: Long,
        deleteFiles: Boolean,
        addImportListExclusion: Boolean
    ): NetworkResult<Unit> =
        delete(
            endpoint = "artist/$id",
            params = mapOf(
                "deleteFiles" to deleteFiles,
                "addImportListExclusion" to addImportListExclusion
            )
        )

    override suspend fun setMonitorStatus(
        id: Long,
        monitorStatus: Boolean
    ): NetworkResult<List<MonitoredResponse>> =
        put("series/editor", mapOf(
            "monitored" to monitorStatus,
            "artistIds" to listOf(id)
        ))

    override suspend fun lookup(query: String): NetworkResult<List<Arrtist>> =
        get<List<Arrtist>>("artist/lookup", mapOf("term" to query))

    override suspend fun addItemToLibrary(item: ArrMedia): NetworkResult<Arrtist> =
        post<ArrMedia, Arrtist>("artist", item)

    override suspend fun performAutomaticSearch(id: Long): NetworkResult<CommandResponse> =
        post("command", CommandPayload.Artist(id))

    override suspend fun getReleases(params: ReleaseParams): NetworkResult<List<LidarrRelease>> {
        if (params !is ReleaseParams.Album) {
            return NetworkResult.Error(message = "Non-lidarr params type $params")
        }

        val params = buildMap<String, Any> {
            params.artistId?.let { put("artistId", it) }
           put("albumId", params.albumId)
        }
        return get("release", params)
    }

    override suspend fun getItemHistory(
        id: Long,
        page: Int,
        pageSize: Int
    ): NetworkResult<List<HistoryItem>> =
        get<LidarrHistoryResponse>("history", mapOf(
            "page" to page,
            "pageSize" to pageSize,
            "albumId" to id
        )).map { it.records }

    override suspend fun getMovieCalendar(
        start: LocalDate,
        end: LocalDate
    ): NetworkResult<List<ArrMovie>> = NetworkResult.Success(emptyList())

    override suspend fun getEpisodeCalendar(
        start: LocalDate,
        end: LocalDate
    ): NetworkResult<List<Episode>> = NetworkResult.Success(emptyList())

    override suspend fun getAlbumCalendar(
        start: LocalDate,
        end: LocalDate
    ): NetworkResult<List<ArrAlbum>> =
        get<List<ArrAlbum>>("calendar", mapOf(
            "start" to start.toString(),
            "end" to end.toString(),
            "unmonitored" to true,
            "includeArtist" to true
        )).map { it.map { ab -> ab.copy(instanceId = instance.id) }}

    suspend fun getAlbums(
        artistId: Long,
        includeAllArtistAlbums: Boolean = true
    ): NetworkResult<List<ArrAlbum>> =
        get("album", mapOf(
            "artistId" to artistId,
            "includeAllArtistAlbums" to includeAllArtistAlbums
        ))

    suspend fun getAlbum(foreignAlbumId: String): NetworkResult<ArrAlbum> =
        get("album", mapOf("foreignAlbumId" to foreignAlbumId))

    suspend fun deleteAlbum(albumId: Long): NetworkResult<Unit> =
        delete("album/$albumId")

    suspend fun getTracks(
        albumId: Long? = null,
        artistId: Long? = null
    ): NetworkResult<List<LidarrTrack>> =
        get("track", buildMap {
            albumId?.let { put("albumId", it) }
            artistId?.let { put("artistId", it) }
        })

    suspend fun getTrackFiles(
        albumId: Long? = null,
        artistId: Long? = null
    ): NetworkResult<List<LidarrTrackFile>> =
        get("trackfile", buildMap {
            albumId?.let { put("albumId", it) }
            artistId?.let { put("artistId", it) }
        })

    suspend fun deleteTracks(trackIds: List<Long>): NetworkResult<Unit> =
        delete(
            endpoint = "trackfile/bulk",
            body = DeleteTrackBody(trackIds)
        )

    suspend fun updateAlbum(album: ArrAlbum): NetworkResult<ArrAlbum> =
        put("album/${album.id}", album)

    suspend fun toggleMonitored(album: ArrAlbum): NetworkResult<ArrAlbum> =
        put<AlbumMonitorBody, List<ArrAlbum>>(
            endpoint = "album/monitor",
            body = AlbumMonitorBody(listOf(album.id), !album.monitored)
        ).map { it.first() }

}