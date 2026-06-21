package com.dnfapps.arrmatey.backup.state

import com.dnfapps.arrmatey.backup.model.BackupExport

data class ImportUiState(
    val password: String = "",
    val decryptedBackup: BackupExport? = null,
    val selectedInstanceIndices: Set<Int> = emptySet(),
    val selectedDownloadClientIndices: Set<Int> = emptySet(),
    val importTabPreferences: Boolean = true,
    val importUiPreferences: Boolean = true,
    val isImporting: Boolean = false,
    val error: String? = null
) {
    constructor(): this("") // empty ios constructor
}