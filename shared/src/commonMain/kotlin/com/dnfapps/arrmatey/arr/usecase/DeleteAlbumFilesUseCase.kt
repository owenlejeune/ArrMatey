package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.networking.onError
import com.dnfapps.networking.onSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DeleteAlbumFilesUseCase {
    operator fun invoke(
        artistId: Long,
        albumId: Long,
        repository: ArrInstanceRepository,
    ): Flow<OperationStatus> =
        flow {
            emit(OperationStatus.InProgress)
            repository
                .deleteAlbumFiles(artistId, albumId)
                .onSuccess {
                    repository.getArtistAlbums(artistId)
                    repository.getArtistTracks(artistId)
                    repository.getArtistTrackFiles(artistId)
                    emit(OperationStatus.Success(message = "Files deleted successfully"))
                }.onError { code, message, cause ->
                    emit(OperationStatus.Error(code, message, cause))
                }
        }
}
