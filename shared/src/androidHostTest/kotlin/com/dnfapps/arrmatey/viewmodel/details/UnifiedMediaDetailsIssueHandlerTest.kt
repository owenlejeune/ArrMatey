package com.dnfapps.arrmatey.viewmodel.details

import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.arrmatey.seerr.api.model.IssueType
import com.dnfapps.arrmatey.seerr.usecase.SubmitIssueUseCase
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class UnifiedMediaDetailsIssueHandlerTest {
    private val submitIssueUseCase: SubmitIssueUseCase = mockk()
    private val handler = UnifiedMediaDetailsIssueHandler(submitIssueUseCase)

    @Test
    fun testSheetVisibilitiesAndSetters() {
        assertFalse(handler.isReportIssueSheetVisible.value)
        handler.showReportIssueSheet()
        assertTrue(handler.isReportIssueSheetVisible.value)
        handler.hideReportIssueSheet()
        assertFalse(handler.isReportIssueSheetVisible.value)

        handler.setIssueType(IssueType.Audio)
        assertEquals(IssueType.Audio, handler.rawReportIssueState.value.issueType)

        handler.setIssueMessage("Bad audio track")
        assertEquals("Bad audio track", handler.rawReportIssueState.value.message)

        handler.setProblemSeason(2)
        assertEquals(2, handler.rawReportIssueState.value.problemSeason)

        handler.setProblemEpisode(5)
        assertEquals(5, handler.rawReportIssueState.value.problemEpisode)

        handler.resetIssueState()
        assertEquals("", handler.rawReportIssueState.value.message)
    }

    @Test
    fun testSubmitIssueSuccess() =
        runTest(UnconfinedTestDispatcher()) {
            every { submitIssueUseCase(any()) } returns flowOf(OperationStatus.Success())

            handler.setIssueMessage("Broken video")
            handler.submitIssue(
                scope = TestScope(UnconfinedTestDispatcher()),
                seerrMediaIdProvider = { 999L },
            )

            assertTrue(handler.rawReportIssueState.value.saveSuccess)
            assertFalse(handler.rawReportIssueState.value.saveInProgress)
        }
}
