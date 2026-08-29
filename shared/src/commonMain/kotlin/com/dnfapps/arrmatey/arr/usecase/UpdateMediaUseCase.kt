package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import com.dnfapps.networking.NetworkResult
import com.dnfapps.networking.onError
import com.dnfapps.networking.onSuccess

class UpdateMediaUseCase {
    suspend operator fun invoke(
        item: ArrMedia,
        repository: ArrInstanceRepository,
    ): NetworkResult<ArrMedia> = repository.updateMediaItem(item)

    suspend fun updateAlbum(
        album: ArrAlbum,
        repository: ArrInstanceRepository,
    ): NetworkResult<ArrAlbum> = repository.updateAlbum(album)

    suspend fun edit(
        item: ArrMedia,
        moveFiles: Boolean,
        repository: ArrInstanceRepository,
    ): NetworkResult<Unit> {
        val id = item.id ?: return repository.editMediaItem(item, moveFiles)

        var previous: Audiobook? = null
        if (item is Audiobook && moveFiles) {
            previous = repository.getCacheMediaDetails(id) as? Audiobook
        }

        return repository
            .editMediaItem(item, moveFiles)
            .onSuccess {
                if (item is Audiobook) {
                    if (moveFiles && previous != null) {
                        val sourcePath = previous.basePath
                        val destinationPath = item.basePath
                        if (sourcePath != null && destinationPath != null && sourcePath != destinationPath) {
                            repository.listenarrClient
                                .moveFiles(
                                    id = id,
                                    moveFiles = true,
                                    sourcePath = sourcePath,
                                    destinationPath = destinationPath,
                                ).onSuccess {
                                    repository.getMediaDetails(id)
                                }
                        }
                    }
                }
            }.onError { code, message, cause ->
                println("$code - $message - ${cause?.printStackTrace()}")
            }
    }

    suspend fun bulkUpdateMonitoring(
        ids: List<Long>,
        monitor: Any,
        repository: ArrInstanceRepository,
    ): NetworkResult<Unit> = repository.updateMonitoring(ids, monitor)
}
