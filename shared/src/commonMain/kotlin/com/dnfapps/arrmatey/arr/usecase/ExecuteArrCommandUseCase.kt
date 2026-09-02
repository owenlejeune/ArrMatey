package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.api.model.CommandPayload
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.usecase.GetArrInstanceRepositoryUseCase
import com.dnfapps.networking.NetworkResult

class ExecuteArrCommandUseCase(
    private val getArrInstanceRepositoryUseCase: GetArrInstanceRepositoryUseCase,
) {
    suspend fun runRssSync(instanceId: Long): NetworkResult<Any> {
        val repository =
            getArrInstanceRepositoryUseCase(instanceId)
                ?: return NetworkResult.Error(message = "Repository not found")
        return repository.executeCommand(CommandPayload.RssSync)
    }

    suspend fun searchAllMissing(instanceId: Long): NetworkResult<Any> {
        val repository =
            getArrInstanceRepositoryUseCase(instanceId)
                ?: return NetworkResult.Error(message = "Repository not found")
        val payload =
            when (repository.instance.type) {
                InstanceType.Sonarr -> CommandPayload.MissingEpisodeSearch
                InstanceType.Radarr -> CommandPayload.MissingMoviesSearch
                InstanceType.Lidarr -> CommandPayload.MissingAlbumSearch
                InstanceType.Bookshelf -> CommandPayload.MissingBookSearch
                else -> return NetworkResult.Error(message = "Unsupported instance type")
            }
        return repository.executeCommand(payload)
    }

    suspend fun updateLibrary(instanceId: Long): NetworkResult<Any> {
        val repository =
            getArrInstanceRepositoryUseCase(instanceId)
                ?: return NetworkResult.Error(message = "Repository not found")
        val payload =
            when (repository.instance.type) {
                InstanceType.Sonarr -> CommandPayload.RefreshSeries()
                InstanceType.Radarr -> CommandPayload.RefreshMovie()
                InstanceType.Lidarr -> CommandPayload.RefreshArtist()
                InstanceType.Bookshelf -> CommandPayload.RefreshAuthor()
                else -> return NetworkResult.Error(message = "Unsupported instance type")
            }
        return repository.executeCommand(payload)
    }

    suspend fun backupDatabase(instanceId: Long): NetworkResult<Any> {
        val repository =
            getArrInstanceRepositoryUseCase(instanceId)
                ?: return NetworkResult.Error(message = "Repository not found")
        return repository.executeCommand(CommandPayload.Backup)
    }
}
