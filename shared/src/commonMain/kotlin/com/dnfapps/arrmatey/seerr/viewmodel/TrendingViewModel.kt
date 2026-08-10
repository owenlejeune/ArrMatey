package com.dnfapps.arrmatey.seerr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.client.paging.PagingController
import com.dnfapps.arrmatey.client.paging.PagingState
import com.dnfapps.arrmatey.client.paging.toPagingState
import com.dnfapps.arrmatey.database.InstanceRepository
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
import com.dnfapps.arrmatey.seerr.api.model.DiscoverResult
import com.dnfapps.arrmatey.seerr.usecase.GetTrendingUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class TrendingViewModel(
    private val instanceManager: InstanceManager,
    private val instanceRepository: InstanceRepository,
    private val getTrendingUseCase: GetTrendingUseCase
) : ViewModel() {

    private val seerrRepository: StateFlow<SeerrInstanceRepository?> = instanceManager.getSelectedSeerrRepository()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val instances: StateFlow<List<Instance>> = instanceRepository.allInstancesFlow
        .map { all -> all.filter { it.type == InstanceType.Seerr } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedInstance: StateFlow<Instance?> = seerrRepository
        .map { it?.instance }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var pagingController: PagingController<DiscoverResult>? = null

    val trendingState: StateFlow<PagingState<DiscoverResult>> = seerrRepository
        .flatMapLatest { repo ->
            if (repo != null) {
                val controller = getTrendingUseCase.createPagingController(repo, viewModelScope)
                pagingController = controller
                controller.loadInitialPage()
                controller.state.map { it.toPagingState() }
            } else {
                pagingController = null
                flowOf(PagingState.Initial)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PagingState.Initial)

    fun loadNextPage() {
        pagingController?.loadNextPage()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            pagingController?.refresh()
            _isRefreshing.value = false
        }
    }
}
