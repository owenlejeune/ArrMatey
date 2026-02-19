package com.dnfapps.arrmatey.di

import com.dnfapps.arrmatey.arr.api.client.DynamicLogger
import com.dnfapps.arrmatey.arr.api.client.GenericClient
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.arr.usecase.AddMediaItemUseCase
import com.dnfapps.arrmatey.instances.usecase.CreateInstanceUseCase
import com.dnfapps.arrmatey.instances.usecase.DeleteInstanceUseCase
import com.dnfapps.arrmatey.instances.usecase.DismissInfoCardUseCase
import com.dnfapps.arrmatey.arr.usecase.DownloadReleaseUseCase
import com.dnfapps.arrmatey.arr.usecase.GetActivityTasksUseCase
import com.dnfapps.arrmatey.instances.usecase.GetInstanceByIdUseCase
import com.dnfapps.arrmatey.instances.usecase.GetInstanceRepositoryUseCase
import com.dnfapps.arrmatey.arr.usecase.GetLibraryUseCase
import com.dnfapps.arrmatey.arr.usecase.GetLookupResultsUseCase
import com.dnfapps.arrmatey.arr.usecase.GetMediaDetailsUseCase
import com.dnfapps.arrmatey.arr.usecase.GetMovieFilesUseCase
import com.dnfapps.arrmatey.arr.usecase.GetReleasesUseCase
import com.dnfapps.arrmatey.instances.usecase.ObserveAllInstancesByTypeUseCase
import com.dnfapps.arrmatey.instances.usecase.ObserveScopedReposByTypeUseCase
import com.dnfapps.arrmatey.instances.usecase.ObserveSelectedInstanceScopedRepoUseCase
import com.dnfapps.arrmatey.instances.usecase.ObserveSelectedInstanceUseCase
import com.dnfapps.arrmatey.arr.usecase.PerformLookupUseCase
import com.dnfapps.arrmatey.instances.usecase.SetInstanceActiveUseCase
import com.dnfapps.arrmatey.instances.usecase.TestInstanceConnectionUseCase
import com.dnfapps.arrmatey.instances.usecase.UpdateInstanceUseCase
import com.dnfapps.arrmatey.instances.usecase.UpdatePreferencesUseCase
import com.dnfapps.arrmatey.arr.viewmodel.ActivityQueueViewModel
import com.dnfapps.arrmatey.arr.viewmodel.AddInstanceViewModel
import com.dnfapps.arrmatey.arr.viewmodel.ArrMediaDetailsViewModel
import com.dnfapps.arrmatey.arr.viewmodel.ArrMediaViewModel
import com.dnfapps.arrmatey.arr.viewmodel.ArrSearchViewModel
import com.dnfapps.arrmatey.arr.viewmodel.EditInstanceViewModel
import com.dnfapps.arrmatey.arr.viewmodel.EpisodeDetailsViewModel
import com.dnfapps.arrmatey.arr.viewmodel.InstancesViewModel
import com.dnfapps.arrmatey.arr.viewmodel.InteractiveSearchViewModel
import com.dnfapps.arrmatey.arr.viewmodel.MediaPreviewViewModel
import com.dnfapps.arrmatey.arr.viewmodel.MoreScreenViewModel
import com.dnfapps.arrmatey.arr.viewmodel.MovieFilesViewModel
import com.dnfapps.arrmatey.arr.service.ActivityQueueService
import com.dnfapps.arrmatey.arr.api.client.HttpClientFactory
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.arr.service.CalendarService
import com.dnfapps.arrmatey.arr.usecase.DeleteAlbumFilesUseCase
import com.dnfapps.arrmatey.arr.usecase.DeleteEpisodeFileUseCase
import com.dnfapps.arrmatey.arr.usecase.DeleteMediaUseCase
import com.dnfapps.arrmatey.arr.usecase.DeleteQueueItemUseCase
import com.dnfapps.arrmatey.arr.usecase.DeleteSeasonFilesUseCase
import com.dnfapps.arrmatey.arr.usecase.GetCalendarUseCase
import com.dnfapps.arrmatey.arr.usecase.GetProwlarrIndexersUseCase
import com.dnfapps.arrmatey.arr.usecase.PerformProwlarrSearchUseCase
import com.dnfapps.arrmatey.arr.usecase.GetEpisodeHistoryUseCase
import com.dnfapps.arrmatey.arr.usecase.PerformAutomaticSearchUseCase
import com.dnfapps.arrmatey.arr.usecase.PerformRefreshUseCase
import com.dnfapps.arrmatey.arr.usecase.ToggleMonitorUseCase
import com.dnfapps.arrmatey.arr.usecase.UpdateMediaUseCase
import com.dnfapps.arrmatey.arr.viewmodel.CalendarViewModel
import com.dnfapps.arrmatey.arr.viewmodel.ProwlarrIndexersViewModel
import com.dnfapps.arrmatey.arr.viewmodel.ProwlarrSearchViewModel
import com.dnfapps.arrmatey.compose.utils.ReleaseFilterBy
import com.dnfapps.arrmatey.database.ArrMateyDatabase
import com.dnfapps.arrmatey.database.InstanceRepository
import com.dnfapps.arrmatey.database.getRoomDatabase
import com.dnfapps.arrmatey.datastore.DataStoreFactory
import com.dnfapps.arrmatey.datastore.InstancePreferenceStoreRepository
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.utils.MokoStrings
import com.dnfapps.arrmatey.utils.NetworkConnectivityObserverFactory
import com.dnfapps.arrmatey.utils.NetworkConnectivityRepository
import io.ktor.client.plugins.logging.Logger
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module
import kotlin.math.sin

val databaseModule = module {
    single { getRoomDatabase(get()) }
    single { get<ArrMateyDatabase>().getInstanceDao() }
}

val networkModule = module {
    single {
        Json {
            isLenient = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    }

    single<Logger> { DynamicLogger(get()) }

    single { HttpClientFactory(get(), get()) }
    single { GenericClient(get()) }

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

    single { InstanceManager(get(), get()) }
}

val serviceModule = module {
    single { ActivityQueueService(get(), get()) }
    single { CalendarService(get()) }
}

val useCaseModule = module {
    factory { GetInstanceRepositoryUseCase(get()) }
    factory { GetLibraryUseCase(get(), get()) }
    factory { GetMediaDetailsUseCase(get()) }
    factory { UpdatePreferencesUseCase(get()) }
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
}

val viewModelModule = module {
    factory { ActivityQueueViewModel(get(), get(), get(), get()) }
    factory { (type: InstanceType) ->
        ArrMediaViewModel(type, get(), get(), get())
    }
    factory { (id: Long, type: InstanceType) ->
        ArrMediaDetailsViewModel(id, type, get(), get(), get(), get(), get(), get(), get(), get(), get())
    }
    factory { (type: InstanceType) ->
        InstancesViewModel(type, get(), get(), get())
    }
    factory { (type: InstanceType) ->
        ArrSearchViewModel(type, get(), get())
    }
    factory { (type: InstanceType) ->
        MediaPreviewViewModel(type, get(), get())
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
    factory { MoreScreenViewModel(get()) }
    factory { AddInstanceViewModel(get(), get(), get(), get()) }
    factory { (instanceId: Long) ->
        EditInstanceViewModel(instanceId, get(), get(), get(), get())
    }
    factory { CalendarViewModel(get(), get()) }
    factory { ProwlarrIndexersViewModel(get(), get()) }
    factory { ProwlarrSearchViewModel(get(), get()) }
}

val resourcesModule = module {
    single { MokoStrings() }
}

expect fun platformModules(): List<Module>

fun appModules() = listOf(networkModule, databaseModule, preferencesModule, repositoryModule, serviceModule, useCaseModule, viewModelModule, resourcesModule) + platformModules()