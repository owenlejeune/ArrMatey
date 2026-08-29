package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.api.model.QueueItem
import com.dnfapps.arrmatey.arr.service.ActivityQueueService
import kotlinx.coroutines.flow.Flow

class GetActivityTasksUseCase(
    private val activityQueueService: ActivityQueueService,
) {
    operator fun invoke(): Flow<List<QueueItem>> = activityQueueService.allActivityTasks

    fun getTasksWithIssues(): Flow<Int> = activityQueueService.tasksWithIssues
}
