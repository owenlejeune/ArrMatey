package com.dnfapps.arrmatey.discover

import com.dnfapps.arrmatey.discover.model.DiscoverFilterState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DiscoverFilterMultiSelectTest {
    @Test
    fun testTogglingGenresAddsAndRemoves() {
        val state = DiscoverFilterState().toggleGenre(28).toggleGenre(35)
        assertEquals(setOf(28, 35), state.selectedGenreIds)
        assertEquals(setOf(35), state.toggleGenre(28).selectedGenreIds)
    }

    @Test
    fun testRemovingLastGenreReturnsToAll() {
        val state = DiscoverFilterState().toggleGenre(28).toggleGenre(28)
        assertTrue(state.selectedGenreIds.isEmpty())
        assertFalse(state.isActive)
    }

    @Test
    fun testAllClearsSelectedGenres() {
        val state = DiscoverFilterState().toggleGenre(28).toggleGenre(35)
        assertTrue(state.copy(selectedGenreIds = emptySet()).matchesGenres(listOf(99)))
    }

    @Test
    fun testGenreMatchesAnySelected() {
        val state = DiscoverFilterState(selectedGenreIds = setOf(28, 35))
        assertTrue(state.matchesGenres(listOf(35, 18)))
        assertTrue(state.matchesGenres(listOf(28)))
        assertFalse(state.matchesGenres(listOf(18)))
        assertFalse(state.matchesGenres(emptyList()))
        assertTrue(DiscoverFilterState().matchesGenres(emptyList()))
    }

    @Test
    fun testLanguageMatchesAnySelectedIgnoringCase() {
        val state = DiscoverFilterState().toggleLanguage("EN").toggleLanguage("ko")
        assertEquals(setOf("en", "ko"), state.selectedLanguages)
        assertTrue(state.matchesLanguage("en"))
        assertTrue(state.matchesLanguage("KO"))
        assertFalse(state.matchesLanguage("fr"))
        assertFalse(state.matchesLanguage(null))
        assertTrue(DiscoverFilterState().matchesLanguage(null))
        assertTrue(state.toggleLanguage("En").toggleLanguage("KO").selectedLanguages.isEmpty())
    }
}
