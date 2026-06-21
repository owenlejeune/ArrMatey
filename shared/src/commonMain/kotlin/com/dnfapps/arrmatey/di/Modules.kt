package com.dnfapps.arrmatey.di

import com.dnfapps.arrmatey.arr.api.client.DynamicLogger
import com.dnfapps.arrmatey.arr.api.client.GenericClient
import com.dnfapps.arrmatey.arr.api.client.HttpClientFactory
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.Book
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.arr.service.ActivityQueueService
import com.dnfapps.arrmatey.arr.service.CalendarService
import com.dnfapps.arrmatey.arr.usecase.AddMediaItemUseCase
import com.dnfapps.arrmatey.arr.usecase.DeleteAlbumFilesUseCase
import com.dnfapps.arrmatey.arr.usecase.DeleteBookFilesUseCase
import com.dnfapps.arrmatey.arr.usecase.DeleteEpisodeFileUseCase
import com.dnfapps.arrmatey.arr.usecase.DeleteMediaUseCase
import com.dnfapps.arrmatey.arr.usecase.DeleteMovieFileUseCase
import com.dnfapps.arrmatey.arr.usecase.DeleteQueueItemUseCase
import com.dnfapps.arrmatey.arr.usecase.DeleteSeasonFilesUseCase
import com.dnfapps.arrmatey.arr.usecase.DownloadReleaseUseCase
import com.dnfapps.arrmatey.arr.usecase.GetActivityTasksUseCase
import com.dnfapps.arrmatey.arr.usecase.GetAudiobookFilesUseCase
import com.dnfapps.arrmatey.arr.usecase.GetAudiobookMetadataUseCase
import com.dnfapps.arrmatey.arr.usecase.GetAudiobookPreviewPathUseCase
import com.dnfapps.arrmatey.arr.usecase.GetAuthorFilesUseCase
import com.dnfapps.arrmatey.arr.usecase.GetBookEditionUseCase
import com.dnfapps.arrmatey.arr.usecase.GetBookHistoryUseCase
import com.dnfapps.arrmatey.arr.usecase.GetCalendarUseCase
import com.dnfapps.arrmatey.arr.usecase.GetEpisodeHistoryUseCase
import com.dnfapps.arrmatey.arr.usecase.GetLibraryUseCase
import com.dnfapps.arrmatey.arr.usecase.GetLookupResultsUseCase
import com.dnfapps.arrmatey.arr.usecase.GetMediaDetailsUseCase
import com.dnfapps.arrmatey.arr.usecase.GetMovieFilesUseCase
import com.dnfapps.arrmatey.arr.usecase.GetProwlarrIndexersStatusUseCase
import com.dnfapps.arrmatey.arr.usecase.GetProwlarrIndexersUseCase
import com.dnfapps.arrmatey.arr.usecase.GetReleasesUseCase
import com.dnfapps.arrmatey.arr.usecase.GrabProwlarrReleaseUseCase
import com.dnfapps.arrmatey.arr.usecase.PerformAutomaticSearchUseCase
import com.dnfapps.arrmatey.arr.usecase.PerformLookupUseCase
import com.dnfapps.arrmatey.arr.usecase.PerformProwlarrSearchUseCase
import com.dnfapps.arrmatey.arr.usecase.PerformRefreshUseCase
import com.dnfapps.arrmatey.arr.usecase.ToggleMonitorUseCase
import com.dnfapps.arrmatey.arr.usecase.UpdateMediaUseCase
import com.dnfapps.arrmatey.arr.viewmodel.ActivityQueueViewModel
import com.dnfapps.arrmatey.arr.viewmodel.AddInstanceViewModel
import com.dnfapps.arrmatey.arr.viewmodel.ArrInstanceDashboardViewModel
import com.dnfapps.arrmatey.arr.viewmodel.ArrMediaDetailsViewModel
import com.dnfapps.arrmatey.arr.viewmodel.ArrMediaViewModel
import com.dnfapps.arrmatey.arr.viewmodel.ArrSearchViewModel
import com.dnfapps.arrmatey.arr.viewmodel.AudiobookFilesViewModel
import com.dnfapps.arrmatey.arr.viewmodel.AuthorFilesViewModel
import com.dnfapps.arrmatey.arr.viewmodel.BookDetailsViewModel
import com.dnfapps.arrmatey.arr.viewmodel.CalendarViewModel
import com.dnfapps.arrmatey.arr.viewmodel.CombinedDashboardViewModel
import com.dnfapps.arrmatey.arr.viewmodel.EditInstanceViewModel
import com.dnfapps.arrmatey.arr.viewmodel.EpisodeDetailsViewModel
import com.dnfapps.arrmatey.arr.viewmodel.InstancesViewModel
import com.dnfapps.arrmatey.arr.viewmodel.InteractiveSearchViewModel
import com.dnfapps.arrmatey.arr.viewmodel.MediaPreviewViewModel
import com.dnfapps.arrmatey.arr.viewmodel.MoreScreenViewModel
import com.dnfapps.arrmatey.arr.viewmodel.MovieFilesViewModel
import com.dnfapps.arrmatey.arr.viewmodel.ProwlarrIndexersViewModel
import com.dnfapps.arrmatey.arr.viewmodel.ProwlarrSearchViewModel
import com.dnfapps.arrmatey.backup.AesTransportEncryptor
import com.dnfapps.arrmatey.backup.TransportEncryptor
import com.dnfapps.arrmatey.backup.usecase.ExportDataUseCase
import com.dnfapps.arrmatey.backup.usecase.ImportDataUseCase
import com.dnfapps.arrmatey.backup.viewmodel.BackupViewModel
import com.dnfapps.arrmatey.compose.DashboardManager
import com.dnfapps.arrmatey.compose.TabManager
import com.dnfapps.arrmatey.compose.utils.ReleaseFilterBy
import com.dnfapps.arrmatey.database.ArrMateyDatabase
import com.dnfapps.arrmatey.database.CredentialMigrationUseCase
import com.dnfapps.arrmatey.database.InstanceRepository
import com.dnfapps.arrmatey.database.getRoomDatabase
import com.dnfapps.arrmatey.datastore.DataStoreFactory
import com.dnfapps.arrmatey.datastore.InstancePreferenceStoreRepository
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.downloadclient.repository.DownloadClientManager
import com.dnfapps.arrmatey.downloadclient.repository.DownloadClientRepository
import com.dnfapps.arrmatey.downloadclient.service.DownloadQueueService
import com.dnfapps.arrmatey.downloadclient.usecase.CreateDownloadClientUseCase
import com.dnfapps.arrmatey.downloadclient.usecase.DeleteDownloadClientUseCase
import com.dnfapps.arrmatey.downloadclient.usecase.DeleteDownloadUseCase
import com.dnfapps.arrmatey.downloadclient.usecase.GetDownloadClientByIdUseCase
import com.dnfapps.arrmatey.downloadclient.usecase.ObserveDownloadClientsUseCase
import com.dnfapps.arrmatey.downloadclient.usecase.ObserveDownloadQueueUseCase
import com.dnfapps.arrmatey.downloadclient.usecase.ObserveSelectedDownloadClientsUseCase
import com.dnfapps.arrmatey.downloadclient.usecase.PauseDownloadUseCase
import com.dnfapps.arrmatey.downloadclient.usecase.RefreshDownloadQueueUseCase
import com.dnfapps.arrmatey.downloadclient.usecase.ResumeDownloadUseCase
import com.dnfapps.arrmatey.downloadclient.usecase.SetDownloadClientActiveUseCase
import com.dnfapps.arrmatey.downloadclient.usecase.TestDownloadClientConnectionUseCase
import com.dnfapps.arrmatey.downloadclient.usecase.UpdateDownloadClientPreferencesUseCase
import com.dnfapps.arrmatey.downloadclient.usecase.UpdateDownloadClientUseCase
import com.dnfapps.arrmatey.downloadclient.viewmodel.DownloadClientSettingsViewModel
import com.dnfapps.arrmatey.downloadclient.viewmodel.DownloadClientsViewModel
import com.dnfapps.arrmatey.downloadclient.viewmodel.DownloadQueueViewModel
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.instances.usecase.CreateInstanceUseCase
import com.dnfapps.arrmatey.instances.usecase.DeleteInstanceUseCase
import com.dnfapps.arrmatey.instances.usecase.DismissInfoCardUseCase
import com.dnfapps.arrmatey.instances.usecase.GetArrInstanceRepositoryUseCase
import com.dnfapps.arrmatey.instances.usecase.GetInstanceByIdUseCase
import com.dnfapps.arrmatey.instances.usecase.GetProwlarrInstanceRepositoryUseCase
import com.dnfapps.arrmatey.instances.usecase.GetSeerrInstanceRepositoryUseCase
import com.dnfapps.arrmatey.instances.usecase.ObserveAllInstancesByTypeUseCase
import com.dnfapps.arrmatey.instances.usecase.ObserveDownloadClientPreferencesUseCase
import com.dnfapps.arrmatey.instances.usecase.ObserveScopedReposByTypeUseCase
import com.dnfapps.arrmatey.instances.usecase.ObserveSelectedInstanceScopedRepoUseCase
import com.dnfapps.arrmatey.instances.usecase.ObserveSelectedInstanceUseCase
import com.dnfapps.arrmatey.instances.usecase.SetInstanceActiveUseCase
import com.dnfapps.arrmatey.instances.usecase.TestInstanceConnectionUseCase
import com.dnfapps.arrmatey.instances.usecase.TestNewInstanceConnectionUseCase
import com.dnfapps.arrmatey.instances.usecase.UpdateCalendarFilterPreferenceUseCase
import com.dnfapps.arrmatey.instances.usecase.UpdateInstancePreferencesUseCase
import com.dnfapps.arrmatey.instances.usecase.UpdateInstanceUseCase
import com.dnfapps.arrmatey.logging.FileSink
import com.dnfapps.arrmatey.notifications.NotificationCleanupUseCase
import com.dnfapps.arrmatey.notifications.ScheduleNotificationUseCase
import com.dnfapps.arrmatey.seerr.api.model.MediaIssuePackage
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.usecase.CancelRequestUseCase
import com.dnfapps.arrmatey.seerr.usecase.CloseIssueUseCase
import com.dnfapps.arrmatey.seerr.usecase.GetCurrentSeerrUserUseCase
import com.dnfapps.arrmatey.seerr.usecase.GetIssueDetailsUseCase
import com.dnfapps.arrmatey.seerr.usecase.GetIssuesUseCase
import com.dnfapps.arrmatey.seerr.usecase.GetRequestsUseCase
import com.dnfapps.arrmatey.seerr.usecase.GetSeerrMediaDetailsUseCase
import com.dnfapps.arrmatey.seerr.usecase.GetSeerrMovieRatingsUseCase
import com.dnfapps.arrmatey.seerr.usecase.GetSeerrTvRatingsUseCase
import com.dnfapps.arrmatey.seerr.usecase.RemoveSeerrMediaFileUseCase
import com.dnfapps.arrmatey.seerr.usecase.SetRequestApprovalStatusUseCase
import com.dnfapps.arrmatey.seerr.usecase.SubmitIssueCommentUseCase
import com.dnfapps.arrmatey.seerr.usecase.SubmitIssueUseCase
import com.dnfapps.arrmatey.seerr.viewmodel.IssueDetailsViewModel
import com.dnfapps.arrmatey.seerr.viewmodel.RequestsViewModel
import com.dnfapps.arrmatey.seerr.viewmodel.SeerrMediaDetailsViewModel
import com.dnfapps.arrmatey.utils.MokoStrings
import com.dnfapps.arrmatey.utils.NetworkConnectivityObserverFactory
import com.dnfapps.arrmatey.utils.NetworkConnectivityRepository
import com.dnfapps.arrmatey.webpage.repository.CustomWebpageRepository
import com.dnfapps.arrmatey.webpage.usecase.AddCustomWebpageUseCase
import com.dnfapps.arrmatey.webpage.usecase.DeleteCustomWebpageUseCase
import com.dnfapps.arrmatey.webpage.usecase.GetCustomWebpageUseCase
import com.dnfapps.arrmatey.webpage.usecase.UpdateCustomWebpageUseCase
import com.dnfapps.arrmatey.webpage.viewmodel.CustomWebpageConfigurationViewModel
import com.dnfapps.arrmatey.webpage.viewmodel.CustomWebpageViewerViewModel
import dev.shivathapaa.logger.api.LogLevel
import dev.shivathapaa.logger.api.LoggerFactory
import dev.shivathapaa.logger.core.LoggerConfig
import dev.shivathapaa.logger.sink.DefaultLogSink
import io.ktor.client.plugins.logging.Logger
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module
import dev.shivathapaa.logger.api.Logger as FLogger

val logging = module {
    single<FLogger> {
        val config = LoggerConfig.Builder()
            .minLevel(LogLevel.DEBUG)
            .addSink(DefaultLogSink())
            .addSink(FileSink("arrmatey.log"))
            .build()

        LoggerFactory.install(config)
        LoggerFactory.get("ArrMatey")
    }
}

val databaseModule = module {
    single { getRoomDatabase(get()) }
    single { get<ArrMateyDatabase>().getInstanceDao() }
    single { get<ArrMateyDatabase>().getDownloadClientDao() }
    single { get<ArrMateyDatabase>().getCustomWebpageDao() }
}

val networkModule = module {
    single {
        Json {
            isLenient = true
            ignoreUnknownKeys = true
            encodeDefaults = true
            explicitNulls = false
        }
    }

    single<Logger> { DynamicLogger(get(), get()) }

    single { HttpClientFactory(get(), get()) }
    single { GenericClient(get()) }

    single<TransportEncryptor> { AesTransportEncryptor() }

    single { NetworkConnectivityObserverFactory().create() }
    single { NetworkConnectivityRepository() }
}

val preferencesModule = module {
    single { DataStoreFactory() }
    single { PreferencesStore(get()) }
}

val repositoryModule = module {
    single { InstanceRepository(get()) }
    single { InstancePreferenceStoreRepository(get()) }
    single { InstanceManager(get(), get(), get(), get()) }

    single { DownloadClientRepository(get()) }
    single { DownloadClientManager(get(), get()) }

    single { CustomWebpageRepository(get()) }

    single { TabManager(get(), get()) }
    single { DashboardManager(get()) }
}

val serviceModule = module {
    single { ActivityQueueService(get(), get()) }
    single { CalendarService(get(), get(), get()) }
    single { DownloadQueueService(get()) }
}

val useCaseModule = module {
    factory { GetArrInstanceRepositoryUseCase(get()) }
    factory { GetLibraryUseCase(get(), get(), get()) }
    factory { GetMediaDetailsUseCase(get()) }
    factory { UpdateInstancePreferencesUseCase(get()) }
    factory { AddMediaItemUseCase(get()) }
    factory { GetActivityTasksUseCase(get()) }
    factory { ObserveAllInstancesByTypeUseCase(get()) }
    factory { ObserveScopedReposByTypeUseCase(get()) }
    factory { ObserveSelectedInstanceScopedRepoUseCase(get()) }
    factory { ObserveSelectedInstanceUseCase(get()) }
    factory { SetInstanceActiveUseCase(get()) }
    factory { GetLookupResultsUseCase(get()) }
    factory { PerformLookupUseCase(get()) }
    factory { AddMediaItemUseCase(get()) }
    factory { GetReleasesUseCase(get()) }
    factory { DownloadReleaseUseCase(get()) }
    factory { GetMovieFilesUseCase(get()) }
    factory { TestInstanceConnectionUseCase(get()) }
    factory { TestNewInstanceConnectionUseCase(get()) }
    factory { CreateInstanceUseCase(get()) }
    factory { UpdateInstanceUseCase(get()) }
    factory { DismissInfoCardUseCase(get()) }
    factory { GetInstanceByIdUseCase(get()) }
    factory { DeleteInstanceUseCase(get()) }
    factory { DeleteSeasonFilesUseCase() }
    factory { ToggleMonitorUseCase() }
    factory { PerformAutomaticSearchUseCase() }
    factory { UpdateMediaUseCase() }
    factory { DeleteMediaUseCase() }
    factory { GetEpisodeHistoryUseCase() }
    factory { DeleteEpisodeFileUseCase() }
    factory { DeleteQueueItemUseCase(get()) }
    factory { PerformRefreshUseCase() }
    factory { GetCalendarUseCase(get()) }
    factory { DeleteAlbumFilesUseCase() }
    factory { GetProwlarrIndexersUseCase(get()) }
    factory { PerformProwlarrSearchUseCase(get()) }
    factory { GrabProwlarrReleaseUseCase(get()) }
    factory { UpdateCalendarFilterPreferenceUseCase(get()) }
    factory { GetSeerrInstanceRepositoryUseCase(get()) }
    factory { GetCurrentSeerrUserUseCase() }
    factory { GetRequestsUseCase() }
    factory { GetIssuesUseCase() }
    factory { GetIssueDetailsUseCase(get()) }
    factory { SubmitIssueUseCase(get()) }
    factory { SubmitIssueCommentUseCase(get()) }
    factory { CancelRequestUseCase() }
    factory { SetRequestApprovalStatusUseCase() }
    factory { RemoveSeerrMediaFileUseCase() }
    factory { GetSeerrMediaDetailsUseCase() }
    factory { GetSeerrMovieRatingsUseCase(get()) }
    factory { GetSeerrTvRatingsUseCase(get()) }
    factory { ObserveDownloadClientsUseCase(get()) }
    factory { ObserveDownloadQueueUseCase(get()) }
    factory { PauseDownloadUseCase(get()) }
    factory { ResumeDownloadUseCase(get()) }
    factory { DeleteDownloadUseCase(get()) }
    factory { TestDownloadClientConnectionUseCase(get()) }
    factory { CreateDownloadClientUseCase(get()) }
    factory { DeleteDownloadClientUseCase(get()) }
    factory { UpdateDownloadClientUseCase(get()) }
    factory { GetDownloadClientByIdUseCase(get()) }
    factory { RefreshDownloadQueueUseCase(get()) }
    factory { ObserveSelectedDownloadClientsUseCase(get()) }
    factory { SetDownloadClientActiveUseCase(get()) }
    factory { GetProwlarrIndexersStatusUseCase(get()) }
    factory { GetProwlarrInstanceRepositoryUseCase(get()) }
    factory { AddCustomWebpageUseCase(get()) }
    factory { UpdateCustomWebpageUseCase(get()) }
    factory { DeleteCustomWebpageUseCase(get()) }
    factory { GetCustomWebpageUseCase(get()) }
    factory { CloseIssueUseCase(get()) }
    factory { NotificationCleanupUseCase(get()) }
    factory { ScheduleNotificationUseCase(get(), get()) }
    factory { DeleteBookFilesUseCase() }
    factory { DeleteMovieFileUseCase() }
    factory { GetAuthorFilesUseCase(get()) }
    factory { GetBookEditionUseCase() }
    factory { GetBookHistoryUseCase() }
    factory { UpdateDownloadClientPreferencesUseCase(get()) }
    factory { ObserveDownloadClientPreferencesUseCase(get()) }
    factory { GetAudiobookFilesUseCase(get()) }
    factory { GetAudiobookPreviewPathUseCase(get()) }
    factory { GetAudiobookMetadataUseCase() }
    factory { CredentialMigrationUseCase(get(), get(), get()) }
    factory { ExportDataUseCase(get(), get(), get(), get(), get(), get()) }
    factory { ImportDataUseCase(get(), get(), get(), get(), get(), get()) }
}

val viewModelModule = module {
    factory { ActivityQueueViewModel(get(), get(), get(), get()) }
    factory { (type: InstanceType) ->
        ArrMediaViewModel(type, get(), get(), get())
    }
    factory { (id: Long, type: InstanceType) ->
        ArrMediaDetailsViewModel(id, type, get(), get(), get(), get(), get(), get(), get(), get(), get(), get())
    }
    factory { (type: InstanceType) ->
        InstancesViewModel(type, get(), get(), get())
    }
    factory { (type: InstanceType) ->
        ArrSearchViewModel(type, get(), get(), get())
    }
    factory { (preview: ArrMedia, type: InstanceType) ->
        MediaPreviewViewModel(preview, type, get(), get(), get(), get())
    }
    factory { (type: InstanceType, defaultFilter: ReleaseFilterBy) ->
        InteractiveSearchViewModel(type, defaultFilter, get(), get(), get())
    }
    factory { (movieId: Long) ->
        MovieFilesViewModel(movieId, get())
    }
    factory { (seriesId: Long, episode: Episode) ->
        EpisodeDetailsViewModel(seriesId, episode, get(), get(), get(), get(), get())
    }
    factory { MoreScreenViewModel(get(), get(), get(), get(), get(), get()) }
    factory { AddInstanceViewModel(get(), get(), get(), get()) }
    factory { (instanceId: Long) ->
        EditInstanceViewModel(instanceId, get(), get(), get(), get(), get())
    }
    factory { (instanceId: Long) ->
        ArrInstanceDashboardViewModel(instanceId, get())
    }
    factory { CalendarViewModel(get(), get(), get(), get()) }
    factory { RequestsViewModel(get(), get(), get(), get(), get(), get(), get()) }
    factory { (tmdbId: Long, mediaType: RequestType) ->
        SeerrMediaDetailsViewModel(tmdbId, mediaType, get(), get(), get(), get(), get(), get(), get())
    }
    factory { ProwlarrIndexersViewModel(get(), get(), get()) }
    factory { ProwlarrSearchViewModel(get(), get(), get()) }
    factory { DownloadQueueViewModel(get(), get(), get(), get(), get(), get(), get()) }
    factory { (clientId: Long?) ->
        DownloadClientSettingsViewModel(clientId, get(), get(), get(), get(), get(), get()) }
    factory { DownloadClientsViewModel(get(), get(), get(), get(), get()) }
    factory { (webpageId: Long?) ->
        CustomWebpageConfigurationViewModel(webpageId, get(), get(), get(), get())
    }
    factory { (webpageId: Long) ->
        CustomWebpageViewerViewModel(webpageId, get(), get())
    }
    factory { (issuePackage: MediaIssuePackage) ->
        IssueDetailsViewModel(issuePackage, get(), get(), get())
    }
    factory { (authorId: Long, book: Book) ->
        BookDetailsViewModel(authorId, book, get(), get(), get(), get(), get(), get())
    }
    factory { (authorId: Long) ->
        AuthorFilesViewModel(authorId, get())
    }
    factory { (audiobookId: Long) ->
        AudiobookFilesViewModel(audiobookId, get())
    }
    factory { CombinedDashboardViewModel(get(), get(), get(), get(), get(), get()) }
    factory { BackupViewModel(get(), get(), get(), get()) }
}

val resourcesModule = module {
    single { MokoStrings() }
}

expect fun platformModules(): List<Module>

fun appModules() = listOf(
    logging,
    networkModule,
    databaseModule,
    preferencesModule,
    repositoryModule,
    serviceModule,
    useCaseModule,
    viewModelModule,
    resourcesModule
) + platformModules()
