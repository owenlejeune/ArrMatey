package com.dnfapps.arrmatey.viewmodel.details

import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.arrmatey.seerr.api.model.ApprovalStatus
import com.dnfapps.arrmatey.seerr.api.model.RequestMediaBody
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.api.model.SeerrUser
import com.dnfapps.arrmatey.seerr.api.model.UserPermission
import com.dnfapps.arrmatey.seerr.usecase.CancelRequestUseCase
import com.dnfapps.arrmatey.seerr.usecase.ClearSeerrMediaDataUseCase
import com.dnfapps.arrmatey.seerr.usecase.MarkSeerrMediaAsAvailableUseCase
import com.dnfapps.arrmatey.seerr.usecase.RemoveSeerrMediaFileUseCase
import com.dnfapps.arrmatey.seerr.usecase.SetRequestApprovalStatusUseCase
import com.dnfapps.arrmatey.seerr.usecase.SubmitRequestUseCase
import com.dnfapps.networking.NetworkResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class UnifiedMediaDetailsSeerrHandlerTest {
    private val submitRequestUseCase: SubmitRequestUseCase = mockk()
    private val cancelRequestUseCase: CancelRequestUseCase = mockk()
    private val setRequestApprovalStatusUseCase: SetRequestApprovalStatusUseCase = mockk()
    private val removeSeerrMediaFileUseCase: RemoveSeerrMediaFileUseCase = mockk()
    private val clearSeerrMediaDataUseCase: ClearSeerrMediaDataUseCase = mockk()
    private val markSeerrMediaAsAvailableUseCase: MarkSeerrMediaAsAvailableUseCase = mockk()

    private val handler =
        UnifiedMediaDetailsSeerrHandler(
            submitRequestUseCase = submitRequestUseCase,
            cancelRequestUseCase = cancelRequestUseCase,
            setRequestApprovalStatusUseCase = setRequestApprovalStatusUseCase,
            removeSeerrMediaFileUseCase = removeSeerrMediaFileUseCase,
            clearSeerrMediaDataUseCase = clearSeerrMediaDataUseCase,
            markSeerrMediaAsAvailableUseCase = markSeerrMediaAsAvailableUseCase,
        )

    @Test
    fun testSheetVisibilities() {
        assertFalse(handler.isRequestSheetVisible.value)
        assertFalse(handler.isViewRequestSheetVisible.value)

        handler.showRequestSheet(is4k = true)
        assertTrue(handler.isRequestSheetVisible.value)
        assertTrue(handler.isRequest4k.value)

        handler.hideRequestSheet()
        assertFalse(handler.isRequestSheetVisible.value)

        handler.showViewRequestSheet()
        assertTrue(handler.isViewRequestSheetVisible.value)

        handler.hideViewRequestSheet()
        assertFalse(handler.isViewRequestSheetVisible.value)
    }

    @Test
    fun testSubmitRequestSuccess() = runTest(UnconfinedTestDispatcher()) {
        val mockRepo = mockk<SeerrInstanceRepository>()
        every { mockRepo.loggedInUser } returns MutableStateFlow(null)
        coEvery { submitRequestUseCase(any(), mockRepo) } returns NetworkResult.Success(mockk())

        var refreshed = false
        handler.showRequestSheet(is4k = false)

        handler.submitRequest(
            scope = TestScope(UnconfinedTestDispatcher()),
            repositoryProvider = { mockRepo },
            resolvedRequestType = RequestType.Movie,
            tmdbId = 12345L,
            onSuccessRefresh = { refreshed = true },
        )

        assertTrue(handler.requestStatus.value is OperationStatus.Success)
        assertFalse(handler.isRequestSheetVisible.value)
        assertTrue(refreshed)
    }

    @Test
    fun testSubmitRequestFiltersSelfUserId() = runTest(UnconfinedTestDispatcher()) {
        val user = SeerrUser(id = 2, displayName = "Test", permissions = UserPermission.ADMIN.bit)
        val mockRepo = mockk<SeerrInstanceRepository>()
        every { mockRepo.loggedInUser } returns MutableStateFlow(user)
        val bodySlot = slot<RequestMediaBody>()
        coEvery { submitRequestUseCase(capture(bodySlot), mockRepo) } returns NetworkResult.Success(mockk())

        handler.submitRequest(
            scope = TestScope(UnconfinedTestDispatcher()),
            repositoryProvider = { mockRepo },
            resolvedRequestType = RequestType.Movie,
            tmdbId = 12345L,
            userId = 2L,
            onSuccessRefresh = {},
        )

        kotlin.test.assertNull(bodySlot.captured.userId)
    }

    @Test
    fun testSubmitRequestFiltersUserIdWithoutPermission() = runTest(UnconfinedTestDispatcher()) {
        val user = SeerrUser(id = 2, displayName = "Test", permissions = 0)
        val mockRepo = mockk<SeerrInstanceRepository>()
        every { mockRepo.loggedInUser } returns MutableStateFlow(user)
        val bodySlot = slot<RequestMediaBody>()
        coEvery { submitRequestUseCase(capture(bodySlot), mockRepo) } returns NetworkResult.Success(mockk())

        handler.submitRequest(
            scope = TestScope(UnconfinedTestDispatcher()),
            repositoryProvider = { mockRepo },
            resolvedRequestType = RequestType.Movie,
            tmdbId = 12345L,
            userId = 3L,
            onSuccessRefresh = {},
        )

        kotlin.test.assertNull(bodySlot.captured.userId)
    }

    @Test
    fun testSubmitRequestIncludesUserIdWithPermission() = runTest(UnconfinedTestDispatcher()) {
        val user = SeerrUser(id = 2, displayName = "Test", permissions = UserPermission.MANAGE_USERS.bit)
        val mockRepo = mockk<SeerrInstanceRepository>()
        every { mockRepo.loggedInUser } returns MutableStateFlow(user)
        val bodySlot = slot<RequestMediaBody>()
        coEvery { submitRequestUseCase(capture(bodySlot), mockRepo) } returns NetworkResult.Success(mockk())

        handler.submitRequest(
            scope = TestScope(UnconfinedTestDispatcher()),
            repositoryProvider = { mockRepo },
            resolvedRequestType = RequestType.Movie,
            tmdbId = 12345L,
            userId = 3L,
            onSuccessRefresh = {},
        )

        kotlin.test.assertEquals(3L, bodySlot.captured.userId)
    }

    @Test
    fun testCancelRequest() = runTest(UnconfinedTestDispatcher()) {
        val mockRepo = mockk<SeerrInstanceRepository>()
        coEvery { cancelRequestUseCase(100L, mockRepo) } returns NetworkResult.Success(Unit)

        var refreshed = false
        handler.cancelRequest(
            scope = TestScope(UnconfinedTestDispatcher()),
            repositoryProvider = { mockRepo },
            requestId = 100L,
            onSuccessRefresh = { refreshed = true },
        )

        assertTrue(handler.requestStatus.value is OperationStatus.Success)
        assertTrue(refreshed)
        coVerify { cancelRequestUseCase(100L, mockRepo) }
    }

    @Test
    fun testDeclineRequest() = runTest(UnconfinedTestDispatcher()) {
        val mockRepo = mockk<SeerrInstanceRepository>()
        coEvery { setRequestApprovalStatusUseCase(200L, ApprovalStatus.Decline, mockRepo) } returns NetworkResult.Success(mockk())

        handler.showViewRequestSheet()
        var refreshed = false

        handler.declineRequest(
            scope = TestScope(UnconfinedTestDispatcher()),
            repositoryProvider = { mockRepo },
            requestId = 200L,
            onSuccessRefresh = { refreshed = true },
        )

        assertTrue(handler.requestStatus.value is OperationStatus.Success)
        assertFalse(handler.isViewRequestSheetVisible.value)
        assertTrue(refreshed)
    }
}
