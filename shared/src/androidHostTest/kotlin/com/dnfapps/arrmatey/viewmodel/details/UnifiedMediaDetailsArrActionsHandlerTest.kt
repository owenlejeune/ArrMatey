package com.dnfapps.arrmatey.viewmodel.details

import com.dnfapps.arrmatey.arr.api.model.ArrMovie
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
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.networking.NetworkResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
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
    fun testToggleMonitored() =
        runTest(UnconfinedTestDispatcher()) {
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
    fun testDeleteMedia() =
        runTest(UnconfinedTestDispatcher()) {
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
    fun testRemoveQueueItem() =
        runTest(UnconfinedTestDispatcher()) {
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
    fun testDismissPendingRequestDialog() {
        handler.dismissPendingRequestDialog()
        assertNull(handler.pendingSeerrRequest.value)
    }
}
