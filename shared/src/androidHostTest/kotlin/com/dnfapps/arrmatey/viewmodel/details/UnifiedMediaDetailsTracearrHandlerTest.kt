package com.dnfapps.arrmatey.viewmodel.details

import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.instances.usecase.GetTracearrInstanceRepositoryUseCase
import com.dnfapps.arrmatey.model.TracearrStatsWindowType
import com.dnfapps.arrmatey.model.UnifiedMediaDetailsUiState
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class UnifiedMediaDetailsTracearrHandlerTest {
    private val handler = UnifiedMediaDetailsTracearrHandler()

    @Test
    fun testSelectTracearrStatsWindow() {
        handler.selectTracearrStatsWindow(TracearrStatsWindowType.Last30)
        assertEquals(TracearrStatsWindowType.Last30, handler.tracearrState.value.selectedStatsWindow)
    }

    @Test
    fun testObserveTracearrDataWhenDisabled() =
        runTest(UnconfinedTestDispatcher()) {
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
}
