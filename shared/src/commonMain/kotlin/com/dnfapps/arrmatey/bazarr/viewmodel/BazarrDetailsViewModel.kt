package com.dnfapps.arrmatey.bazarr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.bazarr.api.model.BazarrMediaType
import com.dnfapps.arrmatey.bazarr.state.BazarrDetails
import com.dnfapps.arrmatey.bazarr.usecase.GetBazarrEpisodesUseCase
import com.dnfapps.arrmatey.bazarr.usecase.GetBazarrMediaDetailsUseCase
import com.dnfapps.arrmatey.bazarr.usecase.PerformBazarrAutomaticSearchUseCase
import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.client.onError
import com.dnfapps.arrmatey.client.onSuccess
import com.dnfapps.arrmatey.instances.usecase.GetBazarrInstanceRepositoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BazarrDetailsViewModel(
    private val id: Long,
    private val mediaType: BazarrMediaType,
    private val getBazarrMediaDetailsUseCase: GetBazarrMediaDetailsUseCase,
    private val getBazarrEpisodesUseCase: GetBazarrEpisodesUseCase,
    private val getBazarrInstanceRepositoryUseCase: GetBazarrInstanceRepositoryUseCase,
    private val performBazarrAutomaticSearchUseCase: PerformBazarrAutomaticSearchUseCase
) : ViewModel() {

    private val _operationState = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val operationState: StateFlow<OperationStatus> = _operationState.asStateFlow()

    val uiState: StateFlow<BazarrDetails> = getBazarrMediaDetailsUseCase(id, mediaType)
        .combine(getBazarrEpisodesUseCase(id)) { details, episodes ->
            BazarrDetails(details, episodes)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BazarrDetails()
        )

    fun performSearch() {
        viewModelScope.launch {
            val repo = getBazarrInstanceRepositoryUseCase.observeSelected().firstOrNull() ?: return@launch
            _operationState.value = OperationStatus.InProgress
            performBazarrAutomaticSearchUseCase(id, mediaType, repo)
                .onSuccess {
                    _operationState.value = OperationStatus.Success()
                }
                .onError { code, message, cause ->
                    _operationState.value = OperationStatus.Error(code, message, cause)
                }
        }
    }

    fun clearOperation() {
        _operationState.value = OperationStatus.Idle
    }
}
