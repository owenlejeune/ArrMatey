package com.dnfapps.arrmatey.seerr.usecase

import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.seerr.api.model.Issue
import kotlinx.coroutines.flow.firstOrNull

class GetIssueDetailsUseCase(
    private val instanceManager: InstanceManager
) {
    suspend operator fun invoke(issueId: Long): NetworkResult<Issue> {
        val repository = instanceManager.getSelectedSeerrRepository()
            .firstOrNull() ?: return NetworkResult.Error(
                message = "No selected seerr repository found"
            )
        return repository.getIssueDetails(issueId)

    }
}