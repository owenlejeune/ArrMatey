package com.dnfapps.arrmatey.tautulli.state

import com.dnfapps.arrmatey.tautulli.api.model.TautulliUser
import com.dnfapps.arrmatey.tautulli.api.model.TautulliUserWatchTime

data class TautulliUserItem(
    val user: TautulliUser,
    val avatarUrl: String? = null,
    val stats: TautulliUserWatchTime? = null,
    val sharedLibraryNames: List<String> = emptyList(),
    val serverName: String = "Plex",
)

sealed class TautulliUsersState {
    data object Initial : TautulliUsersState()
    data object NoInstance : TautulliUsersState()
    data object Loading : TautulliUsersState()
    data class Error(val message: String) : TautulliUsersState()
    data class Success(
        val users: List<TautulliUserItem>,
        val filteredUsers: List<TautulliUserItem> = users,
        val searchQuery: String = "",
    ) : TautulliUsersState()
}
