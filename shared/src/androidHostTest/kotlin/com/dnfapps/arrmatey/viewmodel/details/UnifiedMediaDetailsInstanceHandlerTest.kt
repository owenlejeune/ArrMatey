package com.dnfapps.arrmatey.viewmodel.details

import com.dnfapps.arrmatey.arr.api.model.QualityProfile
import com.dnfapps.arrmatey.arr.api.model.RootFolder
import com.dnfapps.arrmatey.arr.api.model.Tag
import com.dnfapps.arrmatey.arr.service.ActivityQueueService
import com.dnfapps.arrmatey.datastore.InstancePreferences
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import com.dnfapps.arrmatey.instances.usecase.GetArrInstanceRepositoryUseCase
import com.dnfapps.arrmatey.instances.usecase.GetBazarrInstanceRepositoryUseCase
import com.dnfapps.arrmatey.instances.usecase.GetSeerrInstanceRepositoryUseCase
import com.dnfapps.arrmatey.instances.usecase.ObserveInstancePreferencesUseCase
import com.dnfapps.arrmatey.instances.usecase.ObserveScopedReposByTypeUseCase
import com.dnfapps.arrmatey.instances.usecase.UpdateInstancePreferencesUseCase
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class UnifiedMediaDetailsInstanceHandlerTest {
    private val getArrInstanceRepositoryUseCase: GetArrInstanceRepositoryUseCase = mockk(relaxed = true)
    private val getSeerrInstanceRepositoryUseCase: GetSeerrInstanceRepositoryUseCase = mockk(relaxed = true)
    private val getBazarrInstanceRepositoryUseCase: GetBazarrInstanceRepositoryUseCase = mockk(relaxed = true)
    private val observeInstancePreferencesUseCase: ObserveInstancePreferencesUseCase = mockk(relaxed = true)
    private val updateInstancePreferencesUseCase: UpdateInstancePreferencesUseCase = mockk()
    private val observeScopedReposByTypeUseCase: ObserveScopedReposByTypeUseCase = mockk(relaxed = true)
    private val preferencesStore: PreferencesStore = mockk(relaxed = true)
    private val activityQueueService: ActivityQueueService = mockk(relaxed = true)

    @Test
    fun testSetAddSheetTargetInstance() =
        runTest(UnconfinedTestDispatcher()) {
            val mockRepo = mockk<ArrInstanceRepository>(relaxed = true)
            val mockProfiles = MutableStateFlow<List<QualityProfile>>(emptyList())
            val mockFolders = MutableStateFlow<List<RootFolder>>(emptyList())
            val mockTags = MutableStateFlow<List<Tag>>(emptyList())

            every { mockRepo.qualityProfiles } returns mockProfiles
            every { mockRepo.rootFolders } returns mockFolders
            every { mockRepo.tags } returns mockTags
            every { getArrInstanceRepositoryUseCase(10L) } returns mockRepo

            val testScope = TestScope(UnconfinedTestDispatcher())

            every { observeScopedReposByTypeUseCase(InstanceType.Radarr) } returns flowOf(emptyList())
            every { getArrInstanceRepositoryUseCase.observeSelected(InstanceType.Radarr) } returns flowOf(null)
            every { observeInstancePreferencesUseCase(any<Long>()) } returns flowOf(InstancePreferences())

            val handler =
                UnifiedMediaDetailsInstanceHandler(
                    initialForcedInstanceId = 10L,
                    arrId = 1L,
                    resolvedInstanceType = InstanceType.Radarr,
                    resolvedRequestType = null,
                    scope = testScope,
                    getArrInstanceRepositoryUseCase = getArrInstanceRepositoryUseCase,
                    getSeerrInstanceRepositoryUseCase = getSeerrInstanceRepositoryUseCase,
                    getBazarrInstanceRepositoryUseCase = getBazarrInstanceRepositoryUseCase,
                    observeInstancePreferencesUseCase = observeInstancePreferencesUseCase,
                    updateInstancePreferencesUseCase = updateInstancePreferencesUseCase,
                    observeScopedReposByTypeUseCase = observeScopedReposByTypeUseCase,
                    preferencesStore = preferencesStore,
                    activityQueueService = activityQueueService,
                )

            val targetInstance = mockk<Instance>()
            every { targetInstance.id } returns 10L

            handler.setAddSheetTargetInstance(targetInstance)

            assertEquals(targetInstance, handler.addSheetUiState.value.targetInstance)
            coVerify { mockRepo.refreshQualityProfiles() }
            coVerify { mockRepo.refreshRootFolders() }
            coVerify { mockRepo.refreshTags() }
        }
}
