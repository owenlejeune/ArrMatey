package com.dnfapps.arrmatey.seerr.state

import com.dnfapps.arrmatey.seerr.api.model.MediaInfo
import com.dnfapps.arrmatey.seerr.api.model.MediaRequest
import com.dnfapps.arrmatey.seerr.api.model.RequestMedia
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.api.model.RequestUser
import com.dnfapps.arrmatey.seerr.api.model.SeerrUser
import com.dnfapps.arrmatey.seerr.api.model.UserPermission
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MediaButtonStateTest {

    private fun createUser(id: Long = 2L, vararg permissions: UserPermission): SeerrUser = SeerrUser(
        id = id,
        displayName = "Test",
        permissions = permissions.fold(0) { acc, p -> acc or p.bit },
    )

    private fun createRequestUser(id: Long = 2L, vararg permissions: UserPermission): RequestUser = RequestUser(
        id = id,
        displayName = "Test",
        permissions = permissions.fold(0) { acc, p -> acc or p.bit },
    )

    @Test
    fun testRegularUserWithPendingRequestCannotManageOrApprove() {
        val user = createUser(id = 2L, UserPermission.REQUEST)
        val pendingRequest = MediaRequest(
            id = 100L,
            status = 1, // Pending
            media = RequestMedia(id = 10L, tmdbId = 12345L),
            type = RequestType.Movie,
            requestedBy = createRequestUser(id = 2L, UserPermission.REQUEST),
        )
        val mediaInfo = MediaInfo(
            id = 10L,
            tmdbId = 12345L,
            status = 2, // PENDING
            requests = listOf(pendingRequest),
        )

        val buttonState = mediaInfo.toButtonState(
            relatedVideos = emptyList(),
            currentUserId = user.id,
            isAdmin = false,
            currentUser = user,
            mediaType = RequestType.Movie,
        )

        // The regular user can dismiss their own request, but cannot view the approval sheet or approve/decline
        assertTrue(buttonState.showCancelRequestButton)
        assertFalse(buttonState.showViewRequestButton)
        assertFalse(buttonState.showApproveRequestButton)
        assertFalse(buttonState.showDeclineRequestButton)

        // The regular user cannot access manage menu actions
        assertFalse(buttonState.showManageMenu)
        assertFalse(buttonState.showMarkAsAvailableButton)
        assertFalse(buttonState.showRemoveFromServiceButton)
        assertFalse(buttonState.showClearDataButton)
    }

    @Test
    fun testAdminUserWithPendingRequestCanManageAndApprove() {
        val admin = createUser(id = 1L, UserPermission.ADMIN)
        val otherUser = createUser(id = 2L, UserPermission.REQUEST)
        val pendingRequest = MediaRequest(
            id = 100L,
            status = 1, // Pending
            media = RequestMedia(id = 10L, tmdbId = 12345L),
            type = RequestType.Movie,
            requestedBy = createRequestUser(id = 2L, UserPermission.REQUEST),
        )
        val mediaInfo = MediaInfo(
            id = 10L,
            tmdbId = 12345L,
            status = 2, // PENDING
            requests = listOf(pendingRequest),
        )

        val buttonState = mediaInfo.toButtonState(
            relatedVideos = emptyList(),
            currentUserId = admin.id,
            isAdmin = true,
            currentUser = admin,
            mediaType = RequestType.Movie,
        )

        // The admin can view request, approve, and decline
        assertTrue(buttonState.showViewRequestButton)
        assertTrue(buttonState.showApproveRequestButton)
        assertTrue(buttonState.showDeclineRequestButton)
        assertFalse(buttonState.showCancelRequestButton)

        // The admin can manage media
        assertTrue(buttonState.showManageMenu)
        assertTrue(buttonState.showMarkAsAvailableButton)
        assertTrue(buttonState.showRemoveFromServiceButton)
        assertTrue(buttonState.showClearDataButton)
    }

    @Test
    fun testReportIssueButtonRequiresPermission() {
        val availableMedia = MediaInfo(
            id = 10L,
            tmdbId = 12345L,
            status = 5, // AVAILABLE
        )
        val regularUser = createUser(id = 2L, UserPermission.REQUEST)
        val issueCreator = createUser(id = 3L, UserPermission.REQUEST, UserPermission.CREATE_ISSUES)
        val issueManager = createUser(id = 4L, UserPermission.MANAGE_ISSUES)
        val admin = createUser(id = 1L, UserPermission.ADMIN)

        val regularState = availableMedia.toButtonState(
            relatedVideos = emptyList(),
            currentUserId = regularUser.id,
            isAdmin = false,
            currentUser = regularUser,
            mediaType = RequestType.Movie,
        )
        assertFalse(regularState.showReportIssueButton)

        val creatorState = availableMedia.toButtonState(
            relatedVideos = emptyList(),
            currentUserId = issueCreator.id,
            isAdmin = false,
            currentUser = issueCreator,
            mediaType = RequestType.Movie,
        )
        assertTrue(creatorState.showReportIssueButton)

        val managerState = availableMedia.toButtonState(
            relatedVideos = emptyList(),
            currentUserId = issueManager.id,
            isAdmin = false,
            currentUser = issueManager,
            mediaType = RequestType.Movie,
        )
        assertTrue(managerState.showReportIssueButton)

        val adminState = availableMedia.toButtonState(
            relatedVideos = emptyList(),
            currentUserId = admin.id,
            isAdmin = true,
            currentUser = admin,
            mediaType = RequestType.Movie,
        )
        assertTrue(adminState.showReportIssueButton)
    }
}
