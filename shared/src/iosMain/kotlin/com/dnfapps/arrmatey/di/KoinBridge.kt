package com.dnfapps.arrmatey.di

import com.dnfapps.arrmatey.arr.api.client.GenericClient
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.arr.viewmodel.ActivityQueueViewModel
import com.dnfapps.arrmatey.arr.viewmodel.AddInstanceViewModel
import com.dnfapps.arrmatey.arr.viewmodel.ArrInstanceDashboardViewModel
import com.dnfapps.arrmatey.arr.viewmodel.ArrMediaDetailsViewModel
import com.dnfapps.arrmatey.arr.viewmodel.ArrMediaViewModel
import com.dnfapps.arrmatey.arr.viewmodel.ArrSearchViewModel
import com.dnfapps.arrmatey.arr.viewmodel.CalendarViewModel
import com.dnfapps.arrmatey.arr.viewmodel.EditInstanceViewModel
import com.dnfapps.arrmatey.arr.viewmodel.EpisodeDetailsViewModel
import com.dnfapps.arrmatey.arr.viewmodel.InstancesViewModel
import com.dnfapps.arrmatey.arr.viewmodel.InteractiveSearchViewModel
import com.dnfapps.arrmatey.arr.viewmodel.MediaPreviewViewModel
import com.dnfapps.arrmatey.arr.viewmodel.MoreScreenViewModel
import com.dnfapps.arrmatey.arr.viewmodel.MovieFilesViewModel
import com.dnfapps.arrmatey.arr.viewmodel.ProwlarrIndexersViewModel
import com.dnfapps.arrmatey.arr.viewmodel.ProwlarrSearchViewModel
import com.dnfapps.arrmatey.compose.utils.ReleaseFilterBy
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.downloadclient.viewmodel.DownloadClientSettingsViewModel
import com.dnfapps.arrmatey.downloadclient.viewmodel.DownloadClientsViewModel
import com.dnfapps.arrmatey.downloadclient.viewmodel.DownloadQueueViewModel
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.seerr.viewmodel.RequestsViewModel
import com.dnfapps.arrmatey.utils.MokoStrings
import org.koin.core.component.KoinComponent
import org.koin.core.parameter.parametersOf

object KoinBridge: KoinComponent {
    fun getActivityQueueViewModel(): ActivityQueueViewModel =
        getKoin().get()

    fun getArrMediaViewModel(type: InstanceType): ArrMediaViewModel =
        getKoin().get { parametersOf(type) }

    fun getArrMediaDetailsViewModel(id: Long, type: InstanceType): ArrMediaDetailsViewModel =
        getKoin().get { parametersOf(id, type) }

    fun getInstancesViewModel(type: InstanceType): InstancesViewModel =
        getKoin().get { parametersOf(type) }

    fun getArrSearchViewModel(type: InstanceType): ArrSearchViewModel =
        getKoin().get { parametersOf(type) }

    fun getMediaPreviewViewModel(type: InstanceType): MediaPreviewViewModel =
        getKoin().get { parametersOf(type) }

    fun getInteractiveSearchViewModel(type: InstanceType, defaultFilter: ReleaseFilterBy): InteractiveSearchViewModel =
        getKoin().get { parametersOf(type, defaultFilter) }

    fun getMovieFilesViewModel(movieId: Long): MovieFilesViewModel =
        getKoin().get { parametersOf(movieId) }

    fun getEpisodeDetailsViewModel(seriesId: Long, episode: Episode): EpisodeDetailsViewModel =
        getKoin().get { parametersOf(seriesId, episode) }

    fun getMoreScreenViewModel(): MoreScreenViewModel = getKoin().get()

    fun getAddInstanceViewModel(): AddInstanceViewModel = getKoin().get()

    fun getEditInstanceViewModel(instanceId: Long): EditInstanceViewModel =
        getKoin().get { parametersOf(instanceId) }

    fun getCalendarViewModel(): CalendarViewModel =
        getKoin().get()

    fun getArrInstanceDashboardViewModel(instanceId: Long): ArrInstanceDashboardViewModel =
        getKoin().get { parametersOf(instanceId) }

    fun getRequestsViewModel(): RequestsViewModel =
        getKoin().get()

    fun getDownloadQueueViewModel(): DownloadQueueViewModel =
        getKoin().get()

    fun getDownloadClientSettingsViewModel(clientId: Long?): DownloadClientSettingsViewModel =
        getKoin().get { parametersOf(clientId) }

    fun getDownloadClientsViewModel(): DownloadClientsViewModel =
        getKoin().get()
    fun getProwlarrIndexersViewModel(): ProwlarrIndexersViewModel =
        getKoin().get()

    fun getProwlarrSearchViewModel(): ProwlarrSearchViewModel =
        getKoin().get()

    fun getGenericClient(): GenericClient =
        getKoin().get()

    fun getPreferencesStore(): PreferencesStore =
        getKoin().get()

    fun getMokoStrings(): MokoStrings =
        getKoin().get()

}
