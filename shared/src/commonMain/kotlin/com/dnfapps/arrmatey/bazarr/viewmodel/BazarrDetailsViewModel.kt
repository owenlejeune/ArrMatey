package com.dnfapps.arrmatey.bazarr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.bazarr.api.model.BazarrMediaType
import com.dnfapps.arrmatey.bazarr.state.BazarrDetails
import com.dnfapps.arrmatey.bazarr.usecase.GetBazarrEpisodesUseCase
import com.dnfapps.arrmatey.bazarr.usecase.GetBazarrMediaDetailsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class BazarrDetailsViewModel(
    private val id: Long,
    private val mediaType: BazarrMediaType,
    private val getBazarrMediaDetailsUseCase: GetBazarrMediaDetailsUseCase,
    private val getBazarrEpisodesUseCase: GetBazarrEpisodesUseCase
) : ViewModel() {

    val uiState: StateFlow<BazarrDetails> = getBazarrMediaDetailsUseCase(id, mediaType)
        .combine(getBazarrEpisodesUseCase(id)) { details, episodes ->
            BazarrDetails(details, episodes)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BazarrDetails()
        )
}
