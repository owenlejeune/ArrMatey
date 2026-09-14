package com.dnfapps.arrmatey.viewmodel.details

import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
import com.dnfapps.arrmatey.model.UnifiedMediaDetailsUiState
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.api.model.SeerrUser
import com.dnfapps.arrmatey.seerr.api.model.Service
import com.dnfapps.arrmatey.seerr.api.model.ServiceDetails
import com.dnfapps.arrmatey.seerr.api.model.TvDetails
import com.dnfapps.arrmatey.seerr.api.model.UserPermission
import com.dnfapps.arrmatey.seerr.state.MediaButtonState
import com.dnfapps.arrmatey.seerr.state.toButtonState
import com.dnfapps.networking.onSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UnifiedMediaDetailsSeerrServiceHandler(
    private val scope: CoroutineScope,
    private val resolvedRequestType: RequestType?,
    uiStateFlow: Flow<UnifiedMediaDetailsUiState>,
    isSeerrConfiguredFlow: Flow<Boolean>,
) {
    private val _currentUser = MutableStateFlow<SeerrUser?>(null)
    val currentUser: StateFlow<SeerrUser?> = _currentUser.asStateFlow()

    private val _radarrServices = MutableStateFlow<List<Service>>(emptyList())
    val radarrServices: StateFlow<List<Service>> = _radarrServices.asStateFlow()

    private val _sonarrServices = MutableStateFlow<List<Service>>(emptyList())
    val sonarrServices: StateFlow<List<Service>> = _sonarrServices.asStateFlow()

    private val _users = MutableStateFlow<List<SeerrUser>>(emptyList())
    val users: StateFlow<List<SeerrUser>> = _users.asStateFlow()

    private val _serviceDetails = MutableStateFlow<ServiceDetails?>(null)
    val serviceDetails: StateFlow<ServiceDetails?> = _serviceDetails.asStateFlow()

    val buttonState: StateFlow<MediaButtonState> =
        combine(
            uiStateFlow,
            _currentUser,
            isSeerrConfiguredFlow,
            _radarrServices,
            _sonarrServices,
        ) { state, user, isConfigured, radarr, sonarr ->
            when (state) {
                is UnifiedMediaDetailsUiState.Success -> {
                    if (!isConfigured) {
                        MediaButtonState()
                    } else {
                        val isAdmin = user?.hasPermission(UserPermission.ADMIN) == true
                        val totalSeasonCount = (state.seerrMedia as? TvDetails)?.numberOfSeasons ?: 0
                        val rawButtonState =
                            state.seerrMedia?.mediaInfo.toButtonState(
                                state.seerrMedia?.relatedVideos ?: emptyList(),
                                totalSeasonCount,
                                user?.id,
                                isAdmin,
                            )
                        val has4kServer =
                            when (resolvedRequestType) {
                                RequestType.Movie -> radarr.any { it.is4k }
                                RequestType.Tv -> sonarr.any { it.is4k }
                                else -> false
                            }
                        val existsInAnyArr = state.hasArrId || state.presentInstances.isNotEmpty()
                        if (existsInAnyArr) {
                            rawButtonState.copy(
                                showRequestButton = false,
                                showRequestMoreButton = false,
                                showRequest4kButton = false,
                            )
                        } else {
                            rawButtonState.copy(
                                showRequest4kButton = has4kServer && rawButtonState.showRequest4kButton,
                            )
                        }
                    }
                }

                else -> MediaButtonState()
            }
        }.stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MediaButtonState(),
        )

    fun observeSeerrRepo(
        seerrRepoFlow: Flow<SeerrInstanceRepository?>,
        uiStateFlow: Flow<UnifiedMediaDetailsUiState>,
    ) {
        scope.launch {
            seerrRepoFlow.collectLatest { seerrRepo ->
                if (seerrRepo != null) {
                    launch {
                        seerrRepo.getLoggedInUser()
                    }
                    launch {
                        seerrRepo.loggedInUser.collect { _currentUser.value = it }
                    }
                    launch {
                        seerrRepo.getUsers()
                    }
                    launch {
                        seerrRepo.users.collect { _users.value = it }
                    }
                    launch {
                        seerrRepo.getRadarrServices()
                    }
                    launch {
                        seerrRepo.getSonarrServices()
                    }
                    launch {
                        seerrRepo.radarrServices.collect { _radarrServices.value = it }
                    }
                    launch {
                        seerrRepo.sonarrServices.collect { _sonarrServices.value = it }
                    }
                    launch {
                        combine(uiStateFlow, _radarrServices, _sonarrServices) { state, radarr, sonarr ->
                            if (state is UnifiedMediaDetailsUiState.Success) {
                                val request =
                                    state.seerrMedia
                                        ?.mediaInfo
                                        ?.requests
                                        ?.firstOrNull { it.status == 1 }
                                val serverId =
                                    request?.serverId ?: when (resolvedRequestType) {
                                        RequestType.Movie -> radarr.find { it.isDefault }?.id
                                        RequestType.Tv -> sonarr.find { it.isDefault }?.id
                                        else -> null
                                    }
                                if (serverId != null) serverId to resolvedRequestType else null
                            } else {
                                null
                            }
                        }.filterNotNull()
                            .distinctUntilChanged()
                            .collectLatest { (serverId, type) ->
                                val result =
                                    when (type) {
                                        RequestType.Movie -> seerrRepo.getRadarrDetails(serverId)
                                        RequestType.Tv -> seerrRepo.getSonarrDetails(serverId)
                                        else -> return@collectLatest
                                    }
                                result.onSuccess { details ->
                                    _serviceDetails.value = details
                                }
                            }
                    }
                }
            }
        }
    }
}
