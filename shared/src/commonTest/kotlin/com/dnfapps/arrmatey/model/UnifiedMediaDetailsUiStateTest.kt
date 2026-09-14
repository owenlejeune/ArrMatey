package com.dnfapps.arrmatey.model

import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.MockMedia
import com.dnfapps.arrmatey.instances.model.InstanceType
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

    @Test
    fun testCanDeleteFileForAudiobook() {
        val audiobookWithFile = Audiobook(fileCount = 1)
        val stateWithFile = UnifiedMediaDetailsUiState.Success(arrMedia = audiobookWithFile)
        assertTrue(stateWithFile.canDeleteFile(InstanceType.Listenarr))

        val audiobookWithoutFile = Audiobook(fileCount = 0, filePath = null)
        val stateWithoutFile = UnifiedMediaDetailsUiState.Success(arrMedia = audiobookWithoutFile)
        assertFalse(stateWithoutFile.canDeleteFile(InstanceType.Listenarr))
    }

    @Test
    fun testGetAvailableTabsAndDefaultTab() {
        val stateWithSeasons = UnifiedMediaDetailsUiState.Success(
            seasons = listOf(SeasonWrapper(seasonNumber = 1))
        )
        assertEquals(UnifiedMediaDetailsTab.SeasonsFiles, stateWithSeasons.defaultTab)
        assertEquals(
            listOf(UnifiedMediaDetailsTab.SeasonsFiles, UnifiedMediaDetailsTab.Overview),
            stateWithSeasons.getAvailableTabs(isTracearrConfigured = false)
        )

        val stateEmpty = UnifiedMediaDetailsUiState.Success()
        assertEquals(UnifiedMediaDetailsTab.Overview, stateEmpty.defaultTab)
        assertEquals(
            listOf(UnifiedMediaDetailsTab.Overview),
            stateEmpty.getAvailableTabs(isTracearrConfigured = false)
        )
    }
}
