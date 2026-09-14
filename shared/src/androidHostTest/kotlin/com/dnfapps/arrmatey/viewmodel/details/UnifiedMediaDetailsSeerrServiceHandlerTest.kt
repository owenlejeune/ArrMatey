package com.dnfapps.arrmatey.viewmodel.details

import com.dnfapps.arrmatey.model.UnifiedMediaDetailsUiState
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.state.MediaButtonState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class UnifiedMediaDetailsSeerrServiceHandlerTest {
    @Test
    fun testButtonStateInitial() =
        runTest(UnconfinedTestDispatcher()) {
            val handler =
                UnifiedMediaDetailsSeerrServiceHandler(
                    scope = TestScope(UnconfinedTestDispatcher()),
                    resolvedRequestType = RequestType.Movie,
                    uiStateFlow = flowOf(UnifiedMediaDetailsUiState.Initial),
                    isSeerrConfiguredFlow = flowOf(false),
                )

            assertEquals(MediaButtonState(), handler.buttonState.value)
        }
}
