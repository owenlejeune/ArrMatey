package com.dnfapps.arrmatey.seerr.policy

import com.dnfapps.arrmatey.seerr.api.model.MediaRequest
import com.dnfapps.arrmatey.seerr.api.model.RequestMedia
import com.dnfapps.arrmatey.seerr.api.model.RequestStatus
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.api.model.RequestUser
import com.dnfapps.arrmatey.seerr.api.model.SeerrUser
import com.dnfapps.arrmatey.seerr.api.model.UserPermission
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SeerrPermissionPolicyTest {

    private fun createUser(id: Long = 1, vararg permissions: UserPermission): SeerrUser {
        val mask = permissions.fold(0) { acc, p -> acc or p.bit }
        return SeerrUser(
            id = id,
            permissions = mask,
            userType = 1,
            displayName = "Test User",
        )
    }

    private fun createMediaRequest(
        id: Long = 10,
        requesterId: Long = 1,
        status: Int = RequestStatus.Pending.value,
        type: RequestType = RequestType.Movie,
    ): MediaRequest = MediaRequest(
        id = id,
        status = status,
        type = type,
        media = RequestMedia(id = 100),
        requestedBy = RequestUser(id = requesterId),
    )

    @Test
    fun testNullUserHasNoPermissions() {
        assertFalse(SeerrPermissionPolicy.canRequestMedia(null))
        assertFalse(SeerrPermissionPolicy.canRequestMedia(null, is4k = true))
        assertFalse(SeerrPermissionPolicy.willAutoApprove(null, RequestType.Movie))
        assertFalse(SeerrPermissionPolicy.canApproveOrDecline(null))
        assertFalse(SeerrPermissionPolicy.canManageRequests(null))
        assertFalse(SeerrPermissionPolicy.canCancelOrDeleteRequest(null, createMediaRequest()))
        assertFalse(SeerrPermissionPolicy.canConfigureAdvancedSettings(null))
        assertFalse(SeerrPermissionPolicy.canRequestForOtherUsers(null))
        assertFalse(SeerrPermissionPolicy.canViewAllRequests(null))
        assertFalse(SeerrPermissionPolicy.canManageIssues(null))
        assertFalse(SeerrPermissionPolicy.canViewIssues(null))
        assertFalse(SeerrPermissionPolicy.canCreateIssues(null))
    }

    @Test
    fun testAdminBypassesAllRestrictions() {
        val admin = createUser(permissions = arrayOf(UserPermission.ADMIN))

        assertTrue(SeerrPermissionPolicy.canRequestMedia(admin, is4k = false))
        assertTrue(SeerrPermissionPolicy.canRequestMedia(admin, is4k = true))
        assertTrue(SeerrPermissionPolicy.willAutoApprove(admin, RequestType.Movie, is4k = false))
        assertTrue(SeerrPermissionPolicy.willAutoApprove(admin, RequestType.Tv, is4k = false))
        assertTrue(SeerrPermissionPolicy.willAutoApprove(admin, RequestType.Movie, is4k = true))
        assertTrue(SeerrPermissionPolicy.willAutoApprove(admin, RequestType.Tv, is4k = true))
        assertTrue(SeerrPermissionPolicy.canApproveOrDecline(admin))
        assertTrue(SeerrPermissionPolicy.canManageRequests(admin))
        assertTrue(SeerrPermissionPolicy.canCancelOrDeleteRequest(admin, createMediaRequest(requesterId = 999)))
        assertTrue(SeerrPermissionPolicy.canConfigureAdvancedSettings(admin))
        assertTrue(SeerrPermissionPolicy.canRequestForOtherUsers(admin))
        assertTrue(SeerrPermissionPolicy.canViewAllRequests(admin))
        assertTrue(SeerrPermissionPolicy.canManageIssues(admin))
        assertTrue(SeerrPermissionPolicy.canViewIssues(admin))
        assertTrue(SeerrPermissionPolicy.canCreateIssues(admin))
    }

    @Test
    fun testRequestMediaPermissions() {
        val standardUser = createUser(permissions = arrayOf(UserPermission.REQUEST))
        assertTrue(SeerrPermissionPolicy.canRequestMedia(standardUser, is4k = false))
        assertTrue(SeerrPermissionPolicy.canRequestMedia(standardUser, type = RequestType.Movie, is4k = false))
        assertTrue(SeerrPermissionPolicy.canRequestMedia(standardUser, type = RequestType.Tv, is4k = false))
        assertFalse(SeerrPermissionPolicy.canRequestMedia(standardUser, is4k = true))

        val movieOnlyUser = createUser(permissions = arrayOf(UserPermission.REQUEST_MOVIE))
        assertTrue(SeerrPermissionPolicy.canRequestMedia(movieOnlyUser, type = RequestType.Movie, is4k = false))
        assertFalse(SeerrPermissionPolicy.canRequestMedia(movieOnlyUser, type = RequestType.Tv, is4k = false))

        val tvOnlyUser = createUser(permissions = arrayOf(UserPermission.REQUEST_TV))
        assertFalse(SeerrPermissionPolicy.canRequestMedia(tvOnlyUser, type = RequestType.Movie, is4k = false))
        assertTrue(SeerrPermissionPolicy.canRequestMedia(tvOnlyUser, type = RequestType.Tv, is4k = false))

        val user4k = createUser(permissions = arrayOf(UserPermission.REQUEST_4K))
        assertFalse(SeerrPermissionPolicy.canRequestMedia(user4k, is4k = false))
        assertTrue(SeerrPermissionPolicy.canRequestMedia(user4k, is4k = true))
        assertTrue(SeerrPermissionPolicy.canRequestMedia(user4k, type = RequestType.Movie, is4k = true))
        assertTrue(SeerrPermissionPolicy.canRequestMedia(user4k, type = RequestType.Tv, is4k = true))

        val user4kMovieOnly = createUser(permissions = arrayOf(UserPermission.REQUEST_4K_MOVIE))
        assertTrue(SeerrPermissionPolicy.canRequestMedia(user4kMovieOnly, type = RequestType.Movie, is4k = true))
        assertFalse(SeerrPermissionPolicy.canRequestMedia(user4kMovieOnly, type = RequestType.Tv, is4k = true))

        val user4kTvOnly = createUser(permissions = arrayOf(UserPermission.REQUEST_4K_TV))
        assertFalse(SeerrPermissionPolicy.canRequestMedia(user4kTvOnly, type = RequestType.Movie, is4k = true))
        assertTrue(SeerrPermissionPolicy.canRequestMedia(user4kTvOnly, type = RequestType.Tv, is4k = true))

        val bothUser = createUser(permissions = arrayOf(UserPermission.REQUEST, UserPermission.REQUEST_4K))
        assertTrue(SeerrPermissionPolicy.canRequestMedia(bothUser, is4k = false))
        assertTrue(SeerrPermissionPolicy.canRequestMedia(bothUser, is4k = true))
    }

    @Test
    fun testAutoApprovePermissions() {
        val noAuto = createUser(permissions = arrayOf(UserPermission.REQUEST))
        assertFalse(SeerrPermissionPolicy.willAutoApprove(noAuto, RequestType.Movie, is4k = false))
        assertFalse(SeerrPermissionPolicy.willAutoApprove(noAuto, RequestType.Tv, is4k = false))
        assertFalse(SeerrPermissionPolicy.willAutoApprove(noAuto, RequestType.Movie, is4k = true))
        assertFalse(SeerrPermissionPolicy.willAutoApprove(noAuto, RequestType.Tv, is4k = true))

        val autoAll = createUser(permissions = arrayOf(UserPermission.REQUEST, UserPermission.AUTO_APPROVE))
        assertTrue(SeerrPermissionPolicy.willAutoApprove(autoAll, RequestType.Movie, is4k = false))
        assertTrue(SeerrPermissionPolicy.willAutoApprove(autoAll, RequestType.Tv, is4k = false))
        assertFalse(SeerrPermissionPolicy.willAutoApprove(autoAll, RequestType.Movie, is4k = true))

        val autoMovieOnly = createUser(permissions = arrayOf(UserPermission.REQUEST, UserPermission.AUTO_APPROVE_MOVIE))
        assertTrue(SeerrPermissionPolicy.willAutoApprove(autoMovieOnly, RequestType.Movie, is4k = false))
        assertFalse(SeerrPermissionPolicy.willAutoApprove(autoMovieOnly, RequestType.Tv, is4k = false))

        val autoTvOnly = createUser(permissions = arrayOf(UserPermission.REQUEST, UserPermission.AUTO_APPROVE_TV))
        assertFalse(SeerrPermissionPolicy.willAutoApprove(autoTvOnly, RequestType.Movie, is4k = false))
        assertTrue(SeerrPermissionPolicy.willAutoApprove(autoTvOnly, RequestType.Tv, is4k = false))

        val auto4kAll = createUser(permissions = arrayOf(UserPermission.REQUEST_4K, UserPermission.AUTO_APPROVE_4K))
        assertTrue(SeerrPermissionPolicy.willAutoApprove(auto4kAll, RequestType.Movie, is4k = true))
        assertTrue(SeerrPermissionPolicy.willAutoApprove(auto4kAll, RequestType.Tv, is4k = true))
        assertFalse(SeerrPermissionPolicy.willAutoApprove(auto4kAll, RequestType.Movie, is4k = false))

        val auto4kMovieOnly = createUser(permissions = arrayOf(UserPermission.REQUEST_4K, UserPermission.AUTO_APPROVE_4K_MOVIE))
        assertTrue(SeerrPermissionPolicy.willAutoApprove(auto4kMovieOnly, RequestType.Movie, is4k = true))
        assertFalse(SeerrPermissionPolicy.willAutoApprove(auto4kMovieOnly, RequestType.Tv, is4k = true))

        val auto4kTvOnly = createUser(permissions = arrayOf(UserPermission.REQUEST_4K, UserPermission.AUTO_APPROVE_4K_TV))
        assertFalse(SeerrPermissionPolicy.willAutoApprove(auto4kTvOnly, RequestType.Movie, is4k = true))
        assertTrue(SeerrPermissionPolicy.willAutoApprove(auto4kTvOnly, RequestType.Tv, is4k = true))
    }

    @Test
    fun testRequestModerationAndCancellation() {
        val regularUser = createUser(id = 5, permissions = arrayOf(UserPermission.REQUEST))
        val manager = createUser(id = 10, permissions = arrayOf(UserPermission.MANAGE_REQUESTS))

        // Approve/Decline
        assertFalse(SeerrPermissionPolicy.canApproveOrDecline(regularUser))
        assertTrue(SeerrPermissionPolicy.canApproveOrDecline(manager))

        // Manager can cancel any request in any status
        val otherPendingRequest = createMediaRequest(id = 100, requesterId = 999, status = RequestStatus.Pending.value)
        val otherApprovedRequest = createMediaRequest(id = 101, requesterId = 999, status = RequestStatus.Approved.value)
        assertTrue(SeerrPermissionPolicy.canCancelOrDeleteRequest(manager, otherPendingRequest))
        assertTrue(SeerrPermissionPolicy.canCancelOrDeleteRequest(manager, otherApprovedRequest))

        // Regular user can only cancel their OWN pending request
        val ownPendingRequest = createMediaRequest(id = 200, requesterId = 5, status = RequestStatus.Pending.value)
        val ownApprovedRequest = createMediaRequest(id = 201, requesterId = 5, status = RequestStatus.Approved.value)
        assertTrue(SeerrPermissionPolicy.canCancelOrDeleteRequest(regularUser, ownPendingRequest))
        assertFalse(SeerrPermissionPolicy.canCancelOrDeleteRequest(regularUser, ownApprovedRequest))
        assertFalse(SeerrPermissionPolicy.canCancelOrDeleteRequest(regularUser, otherPendingRequest))
    }

    @Test
    fun testAdvancedDetailsPermission() {
        val regularUser = createUser(permissions = arrayOf(UserPermission.REQUEST))
        val advancedUser = createUser(permissions = arrayOf(UserPermission.REQUEST, UserPermission.ADVANCED_DETAILS))

        assertFalse(SeerrPermissionPolicy.canConfigureAdvancedSettings(regularUser))
        assertTrue(SeerrPermissionPolicy.canConfigureAdvancedSettings(advancedUser))
    }

    @Test
    fun testManageUsersPermission() {
        val regularUser = createUser(permissions = arrayOf(UserPermission.REQUEST))
        val userManager = createUser(permissions = arrayOf(UserPermission.REQUEST, UserPermission.MANAGE_USERS))
        val requestManager = createUser(permissions = arrayOf(UserPermission.REQUEST, UserPermission.MANAGE_REQUESTS))

        assertFalse(SeerrPermissionPolicy.canRequestForOtherUsers(regularUser))
        assertTrue(SeerrPermissionPolicy.canRequestForOtherUsers(userManager))
        assertTrue(SeerrPermissionPolicy.canRequestForOtherUsers(requestManager))
    }

    @Test
    fun testViewRequestsPermission() {
        val regularUser = createUser(permissions = arrayOf(UserPermission.REQUEST))
        val viewer = createUser(permissions = arrayOf(UserPermission.VIEW_REQUESTS))
        val manager = createUser(permissions = arrayOf(UserPermission.MANAGE_REQUESTS))

        assertFalse(SeerrPermissionPolicy.canViewAllRequests(regularUser))
        assertTrue(SeerrPermissionPolicy.canViewAllRequests(viewer))
        assertTrue(SeerrPermissionPolicy.canViewAllRequests(manager))
    }

    @Test
    fun testIssuesPermissions() {
        val regularUser = createUser(permissions = arrayOf(UserPermission.REQUEST))
        val viewer = createUser(permissions = arrayOf(UserPermission.VIEW_ISSUES))
        val issueManager = createUser(permissions = arrayOf(UserPermission.MANAGE_ISSUES))
        val issueCreator = createUser(permissions = arrayOf(UserPermission.CREATE_ISSUES))

        assertFalse(SeerrPermissionPolicy.canViewIssues(regularUser))
        assertFalse(SeerrPermissionPolicy.canManageIssues(regularUser))
        assertFalse(SeerrPermissionPolicy.canCreateIssues(regularUser))

        assertTrue(SeerrPermissionPolicy.canViewIssues(viewer))
        assertFalse(SeerrPermissionPolicy.canManageIssues(viewer))
        assertFalse(SeerrPermissionPolicy.canCreateIssues(viewer))

        assertTrue(SeerrPermissionPolicy.canViewIssues(issueManager))
        assertTrue(SeerrPermissionPolicy.canManageIssues(issueManager))
        assertTrue(SeerrPermissionPolicy.canCreateIssues(issueManager))

        assertFalse(SeerrPermissionPolicy.canViewIssues(issueCreator))
        assertFalse(SeerrPermissionPolicy.canManageIssues(issueCreator))
        assertTrue(SeerrPermissionPolicy.canCreateIssues(issueCreator))
    }

    @Test
    fun testCanManageMedia() {
        val admin = createUser(permissions = arrayOf(UserPermission.ADMIN))
        val manager = createUser(permissions = arrayOf(UserPermission.MANAGE_REQUESTS))
        val regularUser = createUser(permissions = arrayOf(UserPermission.REQUEST))
        val noPermUser = createUser(permissions = arrayOf())

        assertTrue(SeerrPermissionPolicy.canManageMedia(admin))
        assertTrue(SeerrPermissionPolicy.canManageMedia(manager))
        assertFalse(SeerrPermissionPolicy.canManageMedia(regularUser))
        assertFalse(SeerrPermissionPolicy.canManageMedia(noPermUser))
        assertFalse(SeerrPermissionPolicy.canManageMedia(null))
    }
}
