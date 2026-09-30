package com.dnfapps.arrmatey.utils

import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.Author
import com.dnfapps.arrmatey.compose.utils.SortBy
import com.dnfapps.arrmatey.compose.utils.SortOrder
import com.dnfapps.arrmatey.compose.utils.alphabet

object FastScrollUtils {
    /**
     * Returns the sort key (title or author sort name) for a media item.
     */
    fun getSortKey(item: ArrMedia, sortBy: SortBy): String {
        val rawKey = when (sortBy) {
            SortBy.TitleLastFirst -> (item as? Author)?.sortNameLastFirst ?: item.sortTitle ?: item.title.orEmpty()
            else -> item.sortTitle ?: item.title.orEmpty()
        }
        return rawKey.trimStart()
    }

    /**
     * Determines the fast scroll section letter ('A'..'Z' or '#') for a media item.
     */
    fun getSectionLetter(item: ArrMedia, sortBy: SortBy): String {
        val firstChar = getSortKey(item, sortBy).firstOrNull()?.uppercaseChar()
        return if (firstChar != null && (firstChar in 'A'..'Z')) firstChar.toString() else "#"
    }

    /**
     * Returns the list of section letters for fast scrolling based on sort order.
     */
    fun getAlphabet(sortOrder: SortOrder): List<String> = sortOrder.alphabet

    /**
     * Builds a map from section letter ("#", "A".."Z") to the first item index in [items].
     */
    fun buildLetterIndexMap(items: List<ArrMedia>, sortBy: SortBy): Map<String, Int> {
        val map = mutableMapOf<String, Int>()
        items.forEachIndexed { index, item ->
            val section = getSectionLetter(item, sortBy)
            if (!map.containsKey(section)) {
                map[section] = index
            }
        }
        return map
    }

    /**
     * Calculates the target item index when a section letter is selected during fast scroll.
     * If [selectedLetter] is not found in [letterIndexMap], it falls back to the next available
     * section in [alphabet], defaulting to the last item index if none is found.
     */
    fun findTargetIndex(
        selectedLetter: String,
        letterIndexMap: Map<String, Int>,
        alphabet: List<String>,
        itemCount: Int,
    ): Int {
        if (itemCount <= 0) return 0
        if (letterIndexMap.containsKey(selectedLetter)) {
            return letterIndexMap.getValue(selectedLetter)
        }
        val startIndex = alphabet.indexOf(selectedLetter)
        if (startIndex != -1) {
            for (i in startIndex until alphabet.size) {
                val candidate = alphabet[i]
                if (letterIndexMap.containsKey(candidate)) {
                    return letterIndexMap.getValue(candidate)
                }
            }
        }
        return (itemCount - 1).coerceAtLeast(0)
    }
}
