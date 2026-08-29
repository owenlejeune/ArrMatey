package com.dnfapps.arrmatey.arr.state

import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrRelease
import com.dnfapps.arrmatey.arr.api.model.CustomFormat
import com.dnfapps.arrmatey.arr.api.model.Language
import com.dnfapps.arrmatey.arr.api.model.QualityInfo
import com.dnfapps.arrmatey.arr.api.model.ReleaseProtocol
import com.dnfapps.arrmatey.datastore.InstancePreferences

sealed interface ArrLibrary {
    object Initial : ArrLibrary

    object Loading : ArrLibrary

    data class Success(
        val items: List<ArrMedia>,
        val preferences: InstancePreferences = InstancePreferences(),
    ) : ArrLibrary

    data class Error(
        val message: String,
        val type: HttpErrorType = HttpErrorType.Http,
    ) : ArrLibrary
}

sealed interface ReleaseLibrary {
    object Initial : ReleaseLibrary

    object Loading : ReleaseLibrary

    data class Success(
        val items: List<ArrRelease>,
        val filterLanguages: Set<Language>,
        val filterQualities: Set<QualityInfo>,
        val filterIndexers: Set<String>,
        val filterProtocols: Set<ReleaseProtocol>,
        val filterCustomFormats: Set<CustomFormat>,
    ) : ReleaseLibrary

    data class Error(
        val message: String,
        val type: HttpErrorType = HttpErrorType.Http,
    ) : ReleaseLibrary
}
