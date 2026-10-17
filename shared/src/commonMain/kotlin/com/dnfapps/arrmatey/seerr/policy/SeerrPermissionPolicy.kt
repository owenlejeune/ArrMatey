package com.dnfapps.arrmatey.seerr.policy

import com.dnfapps.arrmatey.seerr.api.model.MediaRequest
import com.dnfapps.arrmatey.seerr.api.model.RequestStatus
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.api.model.SeerrUser
import com.dnfapps.arrmatey.seerr.api.model.UserPermission

data class SeerrRequestPermissions(
    val canConfigureAdvanced: Boolean = true,
    val canRequestForOtherUsers: Boolean = true,
    val canRequestThis: Boolean = true,
    val willAutoApprove: Boolean = false,
) {
    constructor() : this(true, true, true, false) // Helper for iOS
}

object SeerrPermissionPolicy {

    /**
     * Calculates the aggregate request permissions for a user, media type, and 4K flag.
     */
    fun getRequestPermissions(
        user: SeerrUser?,
        type: RequestType,
        is4k: Boolean = false,
    ): SeerrRequestPermissions = SeerrRequestPermissions(
        canConfigureAdvanced = user == null || canConfigureAdvancedSettings(user),
        canRequestForOtherUsers = user == null || canRequestForOtherUsers(user),
        canRequestThis = user == null || canRequestMedia(user, type, is4k),
        willAutoApprove = user != null && willAutoApprove(user, type, is4k),
    )

    /**
     * Determines whether the user has permission to request the specified media.
     */
    fun canRequestMedia(
        user: SeerrUser?,
        type: RequestType? = null,
        is4k: Boolean = false,
    ): Boolean {
        if (user == null) return false
        if (user.hasPermission(UserPermission.ADMIN)) return true
        return if (is4k) {
            when (type) {
                RequestType.Movie -> user.hasAnyPermission(UserPermission.REQUEST_4K, UserPermission.REQUEST_4K_MOVIE)
                RequestType.Tv -> user.hasAnyPermission(UserPermission.REQUEST_4K, UserPermission.REQUEST_4K_TV)
                else -> user.hasAnyPermission(UserPermission.REQUEST_4K, UserPermission.REQUEST_4K_MOVIE, UserPermission.REQUEST_4K_TV)
            }
        } else {
            when (type) {
                RequestType.Movie -> user.hasAnyPermission(UserPermission.REQUEST, UserPermission.REQUEST_MOVIE)
                RequestType.Tv -> user.hasAnyPermission(UserPermission.REQUEST, UserPermission.REQUEST_TV)
                else -> user.hasAnyPermission(UserPermission.REQUEST, UserPermission.REQUEST_MOVIE, UserPermission.REQUEST_TV)
            }
        }
    }

    fun canRequestMedia(user: SeerrUser?, is4k: Boolean): Boolean = canRequestMedia(user, null, is4k)

    /**
     * Determines whether a request submitted by this user will be auto-approved
     * or placed into the pending moderation queue.
     */
    fun willAutoApprove(user: SeerrUser?, type: RequestType, is4k: Boolean = false): Boolean {
        if (user == null) return false
        if (user.hasPermission(UserPermission.ADMIN)) return true

        return if (is4k) {
            when (type) {
                RequestType.Movie -> user.hasAnyPermission(UserPermission.AUTO_APPROVE_4K, UserPermission.AUTO_APPROVE_4K_MOVIE)
                RequestType.Tv -> user.hasAnyPermission(UserPermission.AUTO_APPROVE_4K, UserPermission.AUTO_APPROVE_4K_TV)
                else -> user.hasPermission(UserPermission.AUTO_APPROVE_4K)
            }
        } else {
            when (type) {
                RequestType.Movie -> user.hasAnyPermission(UserPermission.AUTO_APPROVE, UserPermission.AUTO_APPROVE_MOVIE)
                RequestType.Tv -> user.hasAnyPermission(UserPermission.AUTO_APPROVE, UserPermission.AUTO_APPROVE_TV)
                else -> user.hasPermission(UserPermission.AUTO_APPROVE)
            }
        }
    }

    /**
     * Determines if the user has rights to approve, decline, or edit requests.
     */
    fun canApproveOrDecline(user: SeerrUser?): Boolean {
        if (user == null) return false
        return user.hasAnyPermission(UserPermission.ADMIN, UserPermission.MANAGE_REQUESTS)
    }

    /**
     * Determines if the user has general request management capabilities.
     */
    fun canManageRequests(user: SeerrUser?): Boolean = canApproveOrDecline(user)

    /**
     * Determines whether the user has rights to manage media files/services
     * (mark as available, remove from radarr/sonarr).
     */
    fun canManageMedia(user: SeerrUser?): Boolean {
        if (user == null) return false
        return user.hasAnyPermission(UserPermission.ADMIN, UserPermission.MANAGE_REQUESTS)
    }

    /**
     * Determines whether the user can delete or cancel a request.
     * Managers can delete any request; regular users can cancel their own pending requests.
     */
    fun canCancelOrDeleteRequest(user: SeerrUser?, request: MediaRequest): Boolean {
        if (user == null) return false
        if (canManageRequests(user)) return true
        return request.requestedBy?.id == user.id && request.status == RequestStatus.Pending.value
    }

    /**
     * Determines if the user can choose destination server, quality profile, or root folders.
     */
    fun canConfigureAdvancedSettings(user: SeerrUser?): Boolean {
        if (user == null) return false
        return user.hasAnyPermission(UserPermission.ADMIN, UserPermission.REQUEST_ADVANCED)
    }

    /**
     * Determines if the user can submit a request on behalf of a different user.
     */
    fun canRequestForOtherUsers(user: SeerrUser?): Boolean {
        if (user == null) return false
        return user.hasAnyPermission(
            UserPermission.ADMIN,
            UserPermission.MANAGE_USERS,
            UserPermission.MANAGE_REQUESTS,
        )
    }

    /**
     * Determines if the user can view global request queues.
     */
    fun canViewAllRequests(user: SeerrUser?): Boolean {
        if (user == null) return false
        return user.hasAnyPermission(
            UserPermission.ADMIN,
            UserPermission.MANAGE_REQUESTS,
            UserPermission.REQUEST_VIEW,
        )
    }

    /**
     * Determines if the user can manage (edit, comment, close) media issues.
     */
    fun canManageIssues(user: SeerrUser?): Boolean {
        if (user == null) return false
        return user.hasAnyPermission(UserPermission.ADMIN, UserPermission.MANAGE_ISSUES)
    }

    /**
     * Determines if the user can view reported media issues.
     */
    fun canViewIssues(user: SeerrUser?): Boolean {
        if (user == null) return false
        return user.hasAnyPermission(
            UserPermission.ADMIN,
            UserPermission.MANAGE_ISSUES,
            UserPermission.VIEW_ISSUES,
        )
    }

    /**
     * Determines if the user can report or create issues.
     */
    fun canCreateIssues(user: SeerrUser?): Boolean {
        if (user == null) return false
        return user.hasAnyPermission(
            UserPermission.ADMIN,
            UserPermission.MANAGE_ISSUES,
            UserPermission.CREATE_ISSUES,
        )
    }
}
