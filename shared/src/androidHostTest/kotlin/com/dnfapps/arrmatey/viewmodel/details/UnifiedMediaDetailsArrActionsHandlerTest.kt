package com.dnfapps.arrmatey.viewmodel.details

import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.QueueItem
import com.dnfapps.arrmatey.arr.usecase.DeleteAlbumFilesUseCase
import com.dnfapps.arrmatey.arr.usecase.DeleteAudiobookFileUseCase
import com.dnfapps.arrmatey.arr.usecase.DeleteEpisodeFileUseCase
import com.dnfapps.arrmatey.arr.usecase.DeleteMediaUseCase
import com.dnfapps.arrmatey.arr.usecase.DeleteMovieFileUseCase
import com.dnfapps.arrmatey.arr.usecase.DeleteQueueItemUseCase
import com.dnfapps.arrmatey.arr.usecase.DeleteSeasonFilesUseCase
import com.dnfapps.arrmatey.arr.usecase.PerformAutomaticSearchUseCase
import com.dnfapps.arrmatey.arr.usecase.SmartAddMediaUseCase
import com.dnfapps.arrmatey.arr.usecase.ToggleMonitorUseCase
import com.dnfapps.arrmatey.arr.usecase.UpdateMediaUseCase
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.arrmatey.model.UnifiedMediaDetailsUiState
import com.dnfapps.networking.NetworkResult
import io.mockk.coEvery
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
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class UnifiedMediaDetailsArrActionsHandlerTest {
    private val toggleMonitorUseCase: ToggleMonitorUseCase = mockk()
    private val performAutomaticSearchUseCase: PerformAutomaticSearchUseCase = mockk()
    private val updateMediaUseCase: UpdateMediaUseCase = mockk()
    private val deleteMediaUseCase: DeleteMediaUseCase = mockk()
    private val deleteSeasonFilesUseCase: DeleteSeasonFilesUseCase = mockk()
    private val deleteAlbumFilesUseCase: DeleteAlbumFilesUseCase = mockk()
    private val deleteMovieFileUseCase: DeleteMovieFileUseCase = mockk()
    private val deleteAudiobookFileUseCase: DeleteAudiobookFileUseCase = mockk()
    private val deleteEpisodeFileUseCase: DeleteEpisodeFileUseCase = mockk()
    private val deleteQueueItemUseCase: DeleteQueueItemUseCase = mockk()
    private val smartAddMediaUseCase: SmartAddMediaUseCase = mockk()

    private val handler =
        UnifiedMediaDetailsArrActionsHandler(
            toggleMonitorUseCase = toggleMonitorUseCase,
            performAutomaticSearchUseCase = performAutomaticSearchUseCase,
            updateMediaUseCase = updateMediaUseCase,
            deleteMediaUseCase = deleteMediaUseCase,
            deleteSeasonFilesUseCase = deleteSeasonFilesUseCase,
            deleteAlbumFilesUseCase = deleteAlbumFilesUseCase,
            deleteMovieFileUseCase = deleteMovieFileUseCase,
            deleteAudiobookFileUseCase = deleteAudiobookFileUseCase,
            deleteEpisodeFileUseCase = deleteEpisodeFileUseCase,
            deleteQueueItemUseCase = deleteQueueItemUseCase,
            smartAddMediaUseCase = smartAddMediaUseCase,
        )

    @Test
    fun testToggleMonitored() = runTest(UnconfinedTestDispatcher()) {
        val mockRepo = mockk<ArrInstanceRepository>()
        val mockMedia = mockk<ArrMovie>()
        coEvery { toggleMonitorUseCase.toggleMedia(mockMedia, mockRepo) } returns NetworkResult.Success(mockMedia)

        handler.toggleMonitored(
            scope = TestScope(UnconfinedTestDispatcher()),
            repositoryProvider = { mockRepo },
            mediaProvider = { mockMedia },
        )

        coVerify { toggleMonitorUseCase.toggleMedia(mockMedia, mockRepo) }
    }

    @Test
    fun testDeleteMedia() = runTest(UnconfinedTestDispatcher()) {
        val mockRepo = mockk<ArrInstanceRepository>()
        every { deleteMediaUseCase(123L, true, false, mockRepo) } returns flowOf(OperationStatus.Success())

        handler.deleteMedia(
            scope = TestScope(UnconfinedTestDispatcher()),
            repositoryProvider = { mockRepo },
            effectiveIdProvider = { 123L },
            deleteFiles = true,
            addImportExclusion = false,
        )

        assertTrue(handler.deleteStatus.value is OperationStatus.Success)
    }

    @Test
    fun testRemoveQueueItem() = runTest(UnconfinedTestDispatcher()) {
        val mockQueueItem = mockk<QueueItem>()
        every {
            deleteQueueItemUseCase(
                queueItem = mockQueueItem,
                removeFromClient = true,
                addToBlocklist = false,
                skipRedownload = true,
            )
        } returns flowOf(OperationStatus.Success())

        handler.removeQueueItem(
            scope = TestScope(UnconfinedTestDispatcher()),
            queueItem = mockQueueItem,
            removeFromClient = true,
            addToBlocklist = false,
            skipRedownload = true,
        )

        assertTrue(handler.removeQueueItemStatus.value is OperationStatus.Success)
    }

    @Test
    fun testUpdateMonitoring() = runTest(UnconfinedTestDispatcher()) {
        val mockRepo = mockk<ArrInstanceRepository>()
        coEvery { updateMediaUseCase.bulkUpdateMonitoring(listOf(456L), any(), mockRepo) } returns NetworkResult.Success(Unit)
        coEvery { mockRepo.getMediaDetails(456L) } returns NetworkResult.Success(mockk())

        handler.updateMonitoring(
            scope = TestScope(UnconfinedTestDispatcher()),
            repositoryProvider = { mockRepo },
            effectiveIdProvider = { 456L },
            monitorOption = "none",
        )

        coVerify { updateMediaUseCase.bulkUpdateMonitoring(listOf(456L), "none", mockRepo) }
        assertTrue(handler.editStatus.value is OperationStatus.Success)
    }

    private fun smartAddSeries(
        lookupItem: ArrSeries,
        addedItem: ArrSeries?,
        onItemAdded: (Long, ArrMedia) -> Unit,
    ): ArrInstanceRepository {
        val instance = mockk<Instance> { every { id } returns 7L }
        val repo =
            mockk<ArrInstanceRepository> {
                every { this@mockk.instance } returns instance
                every { addItemStatus } returns MutableStateFlow(OperationStatus.Idle)
            }
        coEvery {
            smartAddMediaUseCase(InstanceType.Sonarr, repo, lookupItem, any(), false)
        } returns addedItem

        handler.smartAdd(
            scope = TestScope(UnconfinedTestDispatcher()),
            resolvedInstanceType = InstanceType.Sonarr,
            requestType = null,
            instanceType = InstanceType.Sonarr,
            item = lookupItem,
            uiState = UnifiedMediaDetailsUiState.Initial,
            preferencesStore = mockk(),
            targetRepoProvider = { repo },
            activeRepoProvider = { repo },
            selectedInstanceIdProvider = { 7L },
            addSheetTargetInstanceIdProvider = { null },
            selectInstance = {},
            refresh = {},
            logger = mockk(relaxed = true),
            onItemAdded = onItemAdded,
        )
        return repo
    }

    @Test
    fun smartAdd_reportsAddedItemSoDetailsCanResolveNewId() = runTest(UnconfinedTestDispatcher()) {
        val lookupItem = mockk<ArrSeries> { every { title } returns "Show" }
        val addedItem = mockk<ArrSeries> { every { id } returns 42L }
        var reported: Pair<Long, ArrMedia>? = null

        smartAddSeries(lookupItem, addedItem) { instanceId, item -> reported = instanceId to item }

        assertEquals(7L to addedItem, reported)
    }

    @Test
    fun smartAdd_doesNotReportWhenAddFails() = runTest(UnconfinedTestDispatcher()) {
        val lookupItem = mockk<ArrSeries> { every { title } returns "Show" }
        var reported = false

        smartAddSeries(lookupItem, addedItem = null) { _, _ -> reported = true }

        assertFalse(reported)
    }

    @Test
    fun testDismissPendingRequestDialog() {
        handler.dismissPendingRequestDialog()
        assertNull(handler.pendingSeerrRequest.value)
    }
}
