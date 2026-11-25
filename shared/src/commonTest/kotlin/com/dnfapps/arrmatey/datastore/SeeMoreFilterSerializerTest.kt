package com.dnfapps.arrmatey.datastore

import com.dnfapps.arrmatey.discover.model.DiscoverFilterState
import com.dnfapps.arrmatey.discover.model.DiscoverSortBy
import com.dnfapps.arrmatey.discover.model.DiscoverSortOrder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeeMoreFilterSerializerTest {
    @Test
    fun testNothingSavedGivesNoFilters() {
        assertTrue(SeeMoreFilterSerializer.deserialize(null).isEmpty())
        assertTrue(SeeMoreFilterSerializer.deserialize("").isEmpty())
        assertTrue(SeeMoreFilterSerializer.deserialize("not json").isEmpty())
    }

    @Test
    fun testSaveAndRestoreEachScreenSeparately() {
        val trending = DiscoverFilterState(
            sortBy = DiscoverSortBy.RATING,
            sortOrder = DiscoverSortOrder.ASCENDING,
            selectedGenreIds = setOf(28, 35),
            selectedLanguages = setOf("en", "ko"),
            hideInLibrary = true,
        )
        val movies = DiscoverFilterState(sortBy = DiscoverSortBy.UPLOAD_TIME)
        val saved = mapOf(
            SeeMoreFilterSerializer.discoverKey("TRENDING") to trending,
            SeeMoreFilterSerializer.newestKey("Movie") to movies,
        )

        val restored = SeeMoreFilterSerializer.deserialize(SeeMoreFilterSerializer.serialize(saved))

        assertEquals(saved, restored)
        assertEquals(null, restored[SeeMoreFilterSerializer.newestKey("Tv")])
    }

    @Test
    fun testUnknownFieldsAndValuesAreTolerated() {
        val json = """{"discover:TRENDING":{"sortBy":"SOMETHING_NEW","selectedGenreIds":[18],"futureField":1}}"""
        val restored = SeeMoreFilterSerializer.deserialize(json)
        assertEquals(DiscoverFilterState(selectedGenreIds = setOf(18)), restored["discover:TRENDING"])
    }

    @Test
    fun testKeys() {
        assertEquals("discover:POPULAR_MOVIES", SeeMoreFilterSerializer.discoverKey("POPULAR_MOVIES"))
        assertEquals("newest:Tv", SeeMoreFilterSerializer.newestKey("Tv"))
    }
}
