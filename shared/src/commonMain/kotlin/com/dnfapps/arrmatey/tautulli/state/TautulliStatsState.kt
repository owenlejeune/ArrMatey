package com.dnfapps.arrmatey.tautulli.state

import com.dnfapps.arrmatey.arr.state.HttpErrorType
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.tautulli.api.model.TautulliHomeStatSection

sealed interface TautulliStatsState {
    data object Initial : TautulliStatsState
    data object Loading : TautulliStatsState
    data class Error(val message: String, val type: HttpErrorType = HttpErrorType.Unexpected) : TautulliStatsState
    data class Success(
        val instance: Instance,
        val sections: List<TautulliHomeStatSection>,
    ) : TautulliStatsState
}
