package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.SerialName

enum class QueueDownloadState {
    @SerialName("downloading")
    Downloading,

    @SerialName("importPending")
    ImportPending,

    @SerialName("importBlocked")
    ImportBlocked,

    @SerialName("importFailed")
    ImportFailed,

    @SerialName("importing")
    Importing,

    @SerialName("imported")
    Imported,

    @SerialName("failedPending")
    FailedPending,

    @SerialName("failed")
    Failed,

    @SerialName("ignored")
    Ignored,

    ;

    fun isManualImport(): Boolean =
        when (this) {
            ImportBlocked, ImportPending -> true
            else -> false
        }
}
