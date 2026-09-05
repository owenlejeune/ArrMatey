package com.dnfapps.arrmatey.model

import com.dnfapps.arrmatey.arr.api.model.SeasonStatistics
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import com.dnfapps.arrmatey.arr.api.model.Season as ArrSeason
import com.dnfapps.arrmatey.seerr.api.model.Season as SeerrSeason

class SeasonWrapperTest {
    private fun arrSeason(
        seasonNumber: Int = 1,
        monitored: Boolean = true,
        statistics: SeasonStatistics? = null,
    ) = ArrSeason(
        seasonNumber = seasonNumber,
        monitored = monitored,
        statistics = statistics,
    )

    private fun seerrSeason(
        seasonNumber: Int = 1,
        name: String = "Indigo League",
        airDate: LocalDate? = LocalDate(1997, 4, 1),
        episodeCount: Int = 82,
    ) = SeerrSeason(
        id = 1,
        seasonNumber = seasonNumber,
        name = name,
        airDate = airDate,
        episodeCount = episodeCount,
    )

    private fun stats(
        totalEpisodeCount: Int = 10,
        episodeFileCount: Int = 5,
        sizeOnDisk: Long = 0,
    ) = SeasonStatistics(
        episodeFileCount = episodeFileCount,
        episodeCount = totalEpisodeCount,
        totalEpisodeCount = totalEpisodeCount,
        sizeOnDisk = sizeOnDisk,
        percentOfEpisodes = 100.0,
    )

    @Test
    fun testTotalEpisodeCountPrefersArrStatistics() {
        val wrapper =
            SeasonWrapper(
                seasonNumber = 1,
                arrSeason = arrSeason(statistics = stats(totalEpisodeCount = 12)),
                seerrSeason = seerrSeason(episodeCount = 8),
                episodes = List(3) { EpisodeWrapper() },
            )
        assertEquals(12, wrapper.totalEpisodeCount)
    }

    @Test
    fun testTotalEpisodeCountFallsBackToSeerr() {
        val wrapper =
            SeasonWrapper(
                seasonNumber = 1,
                seerrSeason = seerrSeason(episodeCount = 8),
                episodes = List(3) { EpisodeWrapper() },
            )
        assertEquals(8, wrapper.totalEpisodeCount)
    }

    @Test
    fun testTotalEpisodeCountFallsBackToEpisodesSize() {
        val wrapper = SeasonWrapper(seasonNumber = 1, episodes = List(4) { EpisodeWrapper() })
        assertEquals(4, wrapper.totalEpisodeCount)
    }

    @Test
    fun testEpisodeFileCountReadsFromArrStatistics() {
        val wrapper =
            SeasonWrapper(
                seasonNumber = 1,
                arrSeason = arrSeason(statistics = stats(episodeFileCount = 7)),
            )
        assertEquals(7, wrapper.episodeFileCount)
    }

    @Test
    fun testEpisodeFileCountNullWithoutArrSeason() {
        val wrapper = SeasonWrapper(seasonNumber = 1, seerrSeason = seerrSeason())
        assertNull(wrapper.episodeFileCount)
    }

    @Test
    fun testActiveEpisodeCount() {
        val episodes =
            listOf(
                EpisodeWrapper(isActive = true),
                EpisodeWrapper(isActive = false),
                EpisodeWrapper(isActive = true),
                EpisodeWrapper(isActive = true),
            )
        val wrapper = SeasonWrapper(seasonNumber = 1, episodes = episodes)
        assertEquals(3, wrapper.activeEpisodeCount)
    }

    @Test
    fun testMonitoredFlagsReflectArrSeason() {
        val monitored = SeasonWrapper(seasonNumber = 1, arrSeason = arrSeason(monitored = true))
        assertTrue(monitored.hasArrSeason)
        assertEquals(true, monitored.monitored)
        assertTrue(monitored.isMonitored)

        val unmonitored = SeasonWrapper(seasonNumber = 1, arrSeason = arrSeason(monitored = false))
        assertEquals(false, unmonitored.monitored)
        assertFalse(unmonitored.isMonitored)
    }

    @Test
    fun testMonitoredIsNullAndFalseWithoutArrSeason() {
        val wrapper = SeasonWrapper(seasonNumber = 1, seerrSeason = seerrSeason())
        assertFalse(wrapper.hasArrSeason)
        assertNull(wrapper.monitored)
        assertFalse(wrapper.isMonitored)
    }

    @Test
    fun testCustomTitleReturnsTrimmedName() {
        val wrapper =
            SeasonWrapper(
                seasonNumber = 1,
                seerrSeason = seerrSeason(name = "  Indigo League  "),
            )
        assertEquals("Indigo League", wrapper.customTitle)
    }

    @Test
    fun testCustomTitleNullWhenNameMatchesSeasonNumber() {
        val wrapper =
            SeasonWrapper(
                seasonNumber = 2,
                seerrSeason = seerrSeason(seasonNumber = 2, name = "Season 2"),
            )
        assertNull(wrapper.customTitle)
    }

    @Test
    fun testCustomTitleNullForSpecialsInSeasonZero() {
        val specials =
            SeasonWrapper(
                seasonNumber = 0,
                seerrSeason = seerrSeason(seasonNumber = 0, name = "Specials"),
            )
        assertNull(specials.customTitle)

        val seasonZero =
            SeasonWrapper(
                seasonNumber = 0,
                seerrSeason = seerrSeason(seasonNumber = 0, name = "Season 0"),
            )
        assertNull(seasonZero.customTitle)
    }

    @Test
    fun testCustomTitleNullWhenNoSeerrSeason() {
        val wrapper = SeasonWrapper(seasonNumber = 1, arrSeason = arrSeason())
        assertNull(wrapper.customTitle)
    }

    @Test
    fun testYearFromSeerrAirDateWhenNoEpisodeDates() {
        val wrapper =
            SeasonWrapper(
                seasonNumber = 1,
                seerrSeason = seerrSeason(airDate = LocalDate(2005, 6, 1)),
            )
        assertEquals("2005", wrapper.year)
    }

    @Test
    fun testInfoStringJoinsAvailableFields() {
        val wrapper =
            SeasonWrapper(
                seasonNumber = 1,
                seerrSeason = seerrSeason(name = "Indigo League", airDate = LocalDate(1997, 4, 1)),
            )
        assertEquals("Indigo League • 1997", wrapper.infoString)
    }

    @Test
    fun testNameExposesSeerrName() {
        val wrapper =
            SeasonWrapper(
                seasonNumber = 1,
                seerrSeason = seerrSeason(name = "Indigo League"),
            )
        assertEquals("Indigo League", wrapper.name)
    }
}
