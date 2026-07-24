package com.dnfapps.arrmatey.bazarr.usecase

import com.dnfapps.arrmatey.bazarr.api.model.BazarrSubtitle
import com.dnfapps.arrmatey.client.onError
import com.dnfapps.arrmatey.client.onSuccess
import com.dnfapps.arrmatey.instances.usecase.GetBazarrInstanceRepositoryUseCase
import com.dnfapps.arrmatey.notifications.NotificationManager
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.MokoStrings
import kotlinx.coroutines.flow.firstOrNull

class DownloadBazarrSubtitleToDeviceUseCase(
    private val getBazarrInstanceRepositoryUseCase: GetBazarrInstanceRepositoryUseCase,
    private val notificationManager: NotificationManager,
    private val mokoStrings: MokoStrings
) {
    suspend operator fun invoke(subtitle: BazarrSubtitle, onResult: (ByteArray?) -> Unit) {
        val path = subtitle.path ?: return
        val fileName = path.substringAfterLast('/')
        val repo = getBazarrInstanceRepositoryUseCase.observeSelected().firstOrNull() ?: return
        val notificationId = path.hashCode()
        val instanceName = repo.instance.label

        repo.getSubtitleFile(path) { progress ->
            notificationManager.showProgressNotification(
                id = notificationId,
                title = mokoStrings.getString(MR.strings.downloading_file, listOf(fileName)),
                message = mokoStrings.getString(MR.strings.downloading_progress),
                progress = progress,
                instanceName = instanceName
            )
        }
            .onSuccess {
                notificationManager.showNotification(
                    id = notificationId,
                    title = mokoStrings.getString(MR.strings.download_complete),
                    message = fileName,
                    instanceName = instanceName
                )
                onResult(it)
            }
            .onError { _, _, _ ->
                notificationManager.cancelNotification(notificationId)
                onResult(null)
            }
    }
}
