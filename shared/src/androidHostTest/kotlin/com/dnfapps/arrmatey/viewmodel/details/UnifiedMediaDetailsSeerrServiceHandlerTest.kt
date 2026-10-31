package com.dnfapps.arrmatey.viewmodel.details

import com.dnfapps.arrmatey.model.UnifiedMediaDetailsUiState
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.state.MediaButtonState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class UnifiedMediaDetailsSeerrServiceHandlerTest {
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        // Keep Dispatchers.Main set so background continuations don't fail when resetting
    }

    @Test
    fun testButtonStateInitial() = runTest(UnconfinedTestDispatcher()) {
        val testJob = kotlinx.coroutines.Job()
        val testScope = kotlinx.coroutines.CoroutineScope(testJob + UnconfinedTestDispatcher())
        try {
            val handler =
                UnifiedMediaDetailsSeerrServiceHandler(
                    scope = testScope,
                    resolvedRequestType = RequestType.Movie,
                    uiStateFlow = flowOf(UnifiedMediaDetailsUiState.Initial),
                    isSeerrConfiguredFlow = flowOf(false),
                )

            assertEquals(MediaButtonState(), handler.buttonState.value)
        } finally {
            testJob.cancel()
        }
    }
}
