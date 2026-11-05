package com.dnfapps.arrmatey.mediaserver.repository

import com.dnfapps.arrmatey.mediaserver.model.UnifiedMediaItem
import com.dnfapps.arrmatey.mediaserver.model.UnifiedMediaLibrary
import kotlinx.coroutines.sync.Mutex

abstract class BaseMediaServerRepository : MediaServerRepository {
    protected val cacheMutex = Mutex()
    protected var cachedLibraries: List<UnifiedMediaLibrary>? = null
    protected val cachedContents = mutableMapOf<String, List<UnifiedMediaItem>>()

    override fun hasCachedLibraries(): Boolean = cachedLibraries != null

    override fun hasCachedContents(libraryId: String): Boolean = cachedContents.containsKey(libraryId)

    override fun hasAllCachedContents(libraryIds: List<String>): Boolean =
        libraryIds.isNotEmpty() && libraryIds.all { cachedContents.containsKey(it) }

    override fun clearCache() {
        cachedLibraries = null
        cachedContents.clear()
    }

    protected fun updateWatchedInCache(itemId: String, watched: Boolean) {
        for ((libId, list) in cachedContents.entries.toList()) {
            cachedContents[libId] = list.map {
                if (it.id == itemId) it.copy(played = watched) else it
            }
        }
    }

    protected fun updateItemInCache(item: UnifiedMediaItem) {
        for ((libId, list) in cachedContents.entries.toList()) {
            cachedContents[libId] = list.map {
                if (it.id == item.id) item else it
            }
        }
    }
}

