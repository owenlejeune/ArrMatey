package com.dnfapps.arrmatey.mediaserver.repository

import com.dnfapps.arrmatey.mediaserver.model.UnifiedMediaItem
import com.dnfapps.arrmatey.mediaserver.model.UnifiedMediaLibrary
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

abstract class BaseMediaServerRepository : MediaServerRepository {
    protected val cacheMutex = Mutex()
    protected var cachedLibraries: List<UnifiedMediaLibrary>? = null
    protected val cachedContents = mutableMapOf<String, List<UnifiedMediaItem>>()

    override suspend fun hasCachedLibraries(): Boolean = cacheMutex.withLock { cachedLibraries != null }

    override suspend fun hasCachedContents(libraryId: String): Boolean = cacheMutex.withLock { cachedContents.containsKey(libraryId) }

    override suspend fun hasAllCachedContents(libraryIds: List<String>): Boolean = cacheMutex.withLock {
        libraryIds.isNotEmpty() && libraryIds.all { cachedContents.containsKey(it) }
    }

    override suspend fun clearCache() {
        cacheMutex.withLock {
            cachedLibraries = null
            cachedContents.clear()
        }
    }

    protected suspend fun updateWatchedInCache(itemId: String, watched: Boolean) {
        cacheMutex.withLock {
            for ((libId, list) in cachedContents.entries.toList()) {
                cachedContents[libId] = list.map {
                    if (it.id == itemId) it.copy(played = watched) else it
                }
            }
        }
    }

    protected suspend fun updateItemInCache(item: UnifiedMediaItem) {
        cacheMutex.withLock {
            for ((libId, list) in cachedContents.entries.toList()) {
                cachedContents[libId] = list.map {
                    if (it.id == item.id) item else it
                }
            }
        }
    }
}
