package com.dnfapps.arrmatey.viewmodel

import com.dnfapps.arrmatey.datastore.InstancePreferences
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.usecase.ObserveInstancePreferencesUseCase
import com.dnfapps.arrmatey.instances.usecase.ObserveScopedReposByTypeUseCase
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test
import kotlin.test.assertEquals

class UnifiedMediaDetailsViewModelTest {
    @Test
    fun testResolvedTypes() {
        val observeInstancePreferencesUseCase = mockk<ObserveInstancePreferencesUseCase>()
        every { observeInstancePreferencesUseCase(any<Long>()) } returns flowOf(InstancePreferences())

        val observeScopedReposByTypeUseCase = mockk<ObserveScopedReposByTypeUseCase>()
        every { observeScopedReposByTypeUseCase(any()) } returns flowOf(emptyList())

        val viewModel =
            UnifiedMediaDetailsViewModel(
                arrId = 1L,
                tmdbId = 1L,
                tvdbId = 1L,
                instanceType = null,
                requestType = RequestType.Movie,
                getUnifiedMediaDetailsUseCase = mockk(relaxed = true),
                smartAddMediaUseCase = mockk(),
                getArrInstanceRepositoryUseCase =
                    mockk(relaxed = true) {
                        every { observeSelected(any()) } returns flowOf(null)
                    },
                getSeerrInstanceRepositoryUseCase =
                    mockk(relaxed = true) {
                        every { observeSelected() } returns flowOf(null)
                    },
                getBazarrInstanceRepositoryUseCase =
                    mockk(relaxed = true) {
                        every { observeSelected() } returns flowOf(null)
                    },
                toggleMonitorUseCase = mockk(),
                updateMediaUseCase = mockk(),
                deleteMediaUseCase = mockk(),
                performRefreshUseCase = mockk(),
                performAutomaticSearchUseCase = mockk(),
                submitRequestUseCase = mockk(),
                cancelRequestUseCase = mockk(),
                setRequestApprovalStatusUseCase = mockk(),
                deleteSeasonFilesUseCase = mockk(),
                deleteAlbumFilesUseCase = mockk(),
                deleteMovieFileUseCase = mockk(),
                submitIssueUseCase = mockk(),
                observeInstancePreferencesUseCase = observeInstancePreferencesUseCase,
                updateInstancePreferencesUseCase = mockk(),
                observeScopedReposByTypeUseCase = observeScopedReposByTypeUseCase,
                getInstancePresencesUseCase = mockk(),
                deleteQueueItemUseCase = mockk(),
                activityQueueService = mockk(relaxed = true),
                removeSeerrMediaFileUseCase = mockk(),
                clearSeerrMediaDataUseCase = mockk(),
                markSeerrMediaAsAvailableUseCase = mockk(),
                logger = mockk(relaxed = true),
            )

        assertEquals(InstanceType.Radarr, viewModel.resolvedInstanceType)
        assertEquals(RequestType.Movie, viewModel.resolvedRequestType)
    }
}
