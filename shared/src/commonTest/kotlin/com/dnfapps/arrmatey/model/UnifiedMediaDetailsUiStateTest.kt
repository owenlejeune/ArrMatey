package com.dnfapps.arrmatey.model

import com.dnfapps.arrmatey.arr.api.model.MockMedia
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UnifiedMediaDetailsUiStateTest {
    @Test
    fun testHasArrId() {
        val successState =
            UnifiedMediaDetailsUiState.Success(
                arrMedia = MockMedia.Default,
            )
        assertTrue(successState.hasArrId)

        val noMediaState =
            UnifiedMediaDetailsUiState.Success(
                arrMedia = null,
            )
        assertFalse(noMediaState.hasArrId)
    }

    @Test
    fun testDisplayTitle() {
        val state =
            UnifiedMediaDetailsUiState.Success(
                arrMedia = MockMedia.Sonarr,
            )
        assertEquals("A Totally Awesome Series", state.displayTitle)
    }

    @Test
    fun testSeasonCountExcludesSpecials() {
        val seasons =
            listOf(
                SeasonWrapper(seasonNumber = 0),
                SeasonWrapper(seasonNumber = 1),
                SeasonWrapper(seasonNumber = 2),
                SeasonWrapper(seasonNumber = 3),
            )
        val state =
            UnifiedMediaDetailsUiState.Success(
                seasons = seasons,
            )
        assertEquals(3, state.seasonCount)
    }

    @Test
    fun testSeasonWrapperCustomTitleAndYear() {
        val seerrSeason =
            com.dnfapps.arrmatey.seerr.api.model.Season(
                id = 123,
                seasonNumber = 1,
                name = "Indigo League",
                airDate = kotlinx.datetime.LocalDate(1997, 4, 1),
                episodeCount = 82,
            )
        val seasonWrapper =
            SeasonWrapper(
                seasonNumber = 1,
                seerrSeason = seerrSeason,
                episodes = emptyList(),
            )

        assertEquals("Indigo League", seasonWrapper.customTitle)
        assertEquals("1997", seasonWrapper.year)
        assertEquals("Indigo League • 1997", seasonWrapper.infoString)
    }
}
