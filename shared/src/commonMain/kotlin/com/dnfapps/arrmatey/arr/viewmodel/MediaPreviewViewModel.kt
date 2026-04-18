package com.dnfapps.arrmatey.arr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.QualityProfile
import com.dnfapps.arrmatey.arr.api.model.RootFolder
import com.dnfapps.arrmatey.arr.api.model.Tag
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import com.dnfapps.arrmatey.arr.usecase.AddMediaItemUseCase
import com.dnfapps.arrmatey.instances.usecase.GetArrInstanceRepositoryUseCase
import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.instances.model.InstanceType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class MediaPreviewViewModel(
    private val instanceType: InstanceType,
    private val getArrInstanceRepositoryUseCase: GetArrInstanceRepositoryUseCase,
    private val addMediaUseCase: AddMediaItemUseCase
): ViewModel() {

    private val _qualityProfiles = MutableStateFlow<List<QualityProfile>>(emptyList())
    val qualityProfiles: StateFlow<List<QualityProfile>> = _qualityProfiles.asStateFlow()

    private val _rootFolders = MutableStateFlow<List<RootFolder>>(emptyList())
    val rootFolders: StateFlow<List<RootFolder>> = _rootFolders.asStateFlow()

    private val _tags = MutableStateFlow<List<Tag>>(emptyList())
    val tags: StateFlow<List<Tag>> = _tags.asStateFlow()

    private val _addItemStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val addItemStatus: StateFlow<OperationStatus> = _addItemStatus.asStateFlow()

    private val _lastAddedItemId = MutableStateFlow<Long?>(null)
    val lastAddedItemId: StateFlow<Long?> = _lastAddedItemId.asStateFlow()

    private var currentRepository: ArrInstanceRepository? = null

    init {
        observeSelectedInstance()
    }

    private fun observeSelectedInstance() {
        viewModelScope.launch {
            getArrInstanceRepositoryUseCase.observeSelected(instanceType)
                .filterNotNull()
                .collectLatest { repository ->
                    currentRepository = repository
                    observeData(repository)
                    repository.refreshAllMetadata()
                }
        }
    }

    private fun observeData(repository: ArrInstanceRepository) {
        viewModelScope.launch {
            repository.qualityProfiles.collect { profiles ->
                _qualityProfiles.emit(profiles)
            }
        }
        viewModelScope.launch {
            repository.rootFolders.collect { rootFolders ->
                _rootFolders.emit(rootFolders)
            }
        }
        viewModelScope.launch {
            repository.tags.collect { tags ->
                _tags.emit(tags)
            }
        }

        viewModelScope.launch {
            repository.addItemStatus.collect { status ->
                _addItemStatus.value = status
            }
        }

        viewModelScope.launch {
            repository.lastAddedItemId.collect { id ->
                _lastAddedItemId.value = id
            }
        }
    }

    fun addItem(item: ArrMedia, searchOnAdd: Boolean) {
        viewModelScope.launch {
            _addItemStatus.value = OperationStatus.InProgress
            addMediaUseCase(instanceType, item, searchOnAdd)
                .collect { state ->
                    _addItemStatus.value = state
                }
        }
    }
}