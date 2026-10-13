package com.dnfapps.arrmatey.viewmodel.details

import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.TautulliRepository
import com.dnfapps.arrmatey.instances.usecase.GetPlaybackMonitoringRepositoryUseCase
import com.dnfapps.arrmatey.instances.usecase.GetTracearrInstanceRepositoryUseCase
import com.dnfapps.arrmatey.model.TracearrStatsWindowType
import com.dnfapps.arrmatey.model.UnifiedMediaDetailsUiState
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.tautulli.api.model.TautulliHistoryItem
import com.dnfapps.arrmatey.tautulli.api.model.TautulliHistoryResponse
import com.dnfapps.arrmatey.tautulli.api.model.TautulliMediaType
import com.dnfapps.networking.NetworkResult
import io.mockk.coEvery
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class UnifiedMediaDetailsTracearrHandlerTest {
    private val handler = UnifiedMediaDetailsTracearrHandler()

    @Test
    fun testSelectTracearrStatsWindow() {
        handler.selectTracearrStatsWindow(TracearrStatsWindowType.Last30)
        assertEquals(TracearrStatsWindowType.Last30, handler.tracearrState.value.selectedStatsWindow)
    }

    @Test
    fun testObserveTracearrDataWhenDisabled() = runTest(UnconfinedTestDispatcher()) {
        val repoUseCase = mockk<GetTracearrInstanceRepositoryUseCase>()
        every { repoUseCase.observeSelected() } returns flowOf(null)

        val prefsStore = mockk<PreferencesStore>()
        every { prefsStore.tracearrDetailsIntegration } returns flowOf(false)

        handler.observeTracearrData(
            scope = TestScope(UnconfinedTestDispatcher()),
            uiStateFlow = flowOf(UnifiedMediaDetailsUiState.Initial),
            getTracearrInstanceRepositoryUseCase = repoUseCase,
            preferencesStore = prefsStore,
            initialTmdbId = 100L,
            initialRequestType = null,
        )

        assertFalse(handler.tracearrState.value.isTracearrConfigured)
    }

    @Test
    fun testObserveMonitoringDataWithTautulli() = runTest(UnconfinedTestDispatcher()) {
        val tautulliRepo = mockk<TautulliRepository>()
        val instance =
            mockk<Instance> {
                every { id } returns 1L
                every { label } returns "Plex"
                every { type } returns InstanceType.Tautulli
                every { url } returns "http://localhost"
            }
        every { tautulliRepo.instance } returns instance
        val tautulliSession =
            com.dnfapps.arrmatey.tautulli.api.model.TautulliSession(
                sessionKey = "sess1",
                title = "Inception",
                mediaType = TautulliMediaType.Movie,
                user = "alice",
                player = "Plex Web",
            )
        every { tautulliRepo.activeStreams } returns MutableStateFlow(listOf(tautulliSession))
        every { tautulliRepo.resolveServerName(any()) } returns "HomePlex"
        every { tautulliRepo.buildPmsImageUrl(any()) } returns null
        coEvery { tautulliRepo.refreshStreams() } returns NetworkResult.Error(code = 500, message = "error")

        val historyItem =
            TautulliHistoryItem(
                id = 10L,
                user = "alice",
                title = "Inception",
                mediaType = TautulliMediaType.Movie,
                year = 2010,
                duration = 7200L,
                percentComplete = 100,
                date = 1700000000L,
                serverName = "HomePlex",
            )
        val historyResponse =
            TautulliHistoryResponse(
                recordsTotal = 1,
                recordsFiltered = 1,
                data = listOf(historyItem),
            )
        coEvery {
            tautulliRepo.getHistory(
                start = any(),
                length = any(),
                search = any(),
                userId = null,
                ratingKey = null,
                mediaType = any(),
            )
        } returns NetworkResult.Success(historyResponse)
        coEvery {
            tautulliRepo.getHistory(
                start = any(),
                length = any(),
                search = any(),
                userId = null,
                ratingKey = null,
                mediaType = null,
            )
        } returns NetworkResult.Success(historyResponse)

        val playbackUseCase = mockk<GetPlaybackMonitoringRepositoryUseCase>()
        every { playbackUseCase.observeAll() } returns flowOf(listOf(tautulliRepo))
        every { playbackUseCase.observeSelected() } returns flowOf(tautulliRepo)

        val tracearrUseCase = mockk<GetTracearrInstanceRepositoryUseCase>()
        every { tracearrUseCase.observeSelected() } returns flowOf(null)

        val prefsStore = mockk<PreferencesStore>()
        every { prefsStore.tracearrDetailsIntegration } returns flowOf(true)

        val movie =
            mockk<ArrMovie> {
                every { id } returns 1L
                every { title } returns "Inception"
                every { year } returns 2010
                every { tmdbId } returns 27205
            }
        val successState = UnifiedMediaDetailsUiState.Success(arrMedia = movie)

        handler.observeTracearrData(
            scope = backgroundScope,
            uiStateFlow = flowOf(successState),
            getTracearrInstanceRepositoryUseCase = tracearrUseCase,
            getPlaybackMonitoringRepositoryUseCase = playbackUseCase,
            preferencesStore = prefsStore,
            initialTmdbId = 27205L,
            initialRequestType = RequestType.Movie,
        )

        val state = handler.tracearrState.value
        assertTrue(state.isTracearrConfigured)
        assertEquals(1, state.historyItems.size)
        assertEquals("Inception", state.historyItems.first().mediaTitle)
        assertEquals(1, state.activeStreams.size)
        assertEquals("Inception", state.activeStreams.first().mediaTitle)
        assertEquals(1L, state.totalPlays)
        assertEquals(7200000L, state.totalWatchTimeMs)
        assertEquals(1, state.uniqueUsers)
    }
}
