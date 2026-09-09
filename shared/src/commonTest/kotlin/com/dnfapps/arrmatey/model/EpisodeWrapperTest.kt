package com.dnfapps.arrmatey.model

import com.dnfapps.arrmatey.arr.api.model.EpisodeFile
import com.dnfapps.arrmatey.arr.api.model.Quality
import com.dnfapps.arrmatey.arr.api.model.QualityInfo
import com.dnfapps.arrmatey.arr.api.model.Revision
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import com.dnfapps.arrmatey.arr.api.model.Episode as ArrEpisode
import com.dnfapps.arrmatey.seerr.api.model.Episode as SeerrEpisode

class EpisodeWrapperTest {
    private fun arrEpisode(
        seasonNumber: Int = 1,
        episodeNumber: Int = 3,
        title: String? = "Arr Title",
        overview: String? = "Arr overview",
        monitored: Boolean = true,
        hasFile: Boolean = false,
        airDate: LocalDate? = LocalDate(2020, 1, 15),
        file: EpisodeFile? = null,
    ) = ArrEpisode(
        id = 42,
        seriesId = 7,
        tvdbId = null,
        episodeFileId = file?.id,
        seasonNumber = seasonNumber,
        episodeNumber = episodeNumber,
        title = title,
        airDate = airDate,
        runtime = 30,
        overview = overview,
        episodeFile = file,
        hasFile = hasFile,
        monitored = monitored,
        unverifiedSceneNumbering = false,
    )

    private fun seerrEpisode(
        seasonNumber: Int = 2,
        episodeNumber: Int = 5,
        name: String = "Seerr Name",
        overview: String? = "Seerr overview",
        stillPath: String? = "/still.jpg",
    ) = SeerrEpisode(
        id = 100,
        name = name,
        airDate = null,
        episodeNumber = episodeNumber,
        overview = overview,
        seasonNumber = seasonNumber,
        showId = 1,
        stillPath = stillPath,
    )

    @Test
    fun testPrefersArrEpisodeForNumbersAndTitle() {
        val wrapper = EpisodeWrapper(arrEpisode = arrEpisode(), seerrEpisode = seerrEpisode())
        assertEquals(1, wrapper.seasonNumber)
        assertEquals(3, wrapper.episodeNumber)
        assertEquals("Arr Title", wrapper.title)
        assertEquals("Arr overview", wrapper.overview)
    }

    @Test
    fun testFallsBackToSeerrEpisodeWhenArrMissing() {
        val wrapper = EpisodeWrapper(seerrEpisode = seerrEpisode())
        assertEquals(2, wrapper.seasonNumber)
        assertEquals(5, wrapper.episodeNumber)
        assertEquals("Seerr Name", wrapper.title)
        assertEquals("Seerr overview", wrapper.overview)
    }

    @Test
    fun testDefaultsWhenNoEpisodesProvided() {
        val wrapper = EpisodeWrapper()
        assertEquals(0, wrapper.seasonNumber)
        assertEquals(0, wrapper.episodeNumber)
        assertNull(wrapper.title)
        assertNull(wrapper.overview)
        assertNull(wrapper.stillPath)
        assertNull(wrapper.airDate)
        assertNull(wrapper.monitored)
        assertFalse(wrapper.isMonitored)
        assertFalse(wrapper.hasFile)
        assertNull(wrapper.fileQualityName)
        assertFalse(wrapper.isActive)
    }

    @Test
    fun testStillPathPrefersSeerr() {
        val wrapper =
            EpisodeWrapper(
                arrEpisode = arrEpisode(),
                seerrEpisode = seerrEpisode(stillPath = "/seerr.jpg"),
            )
        assertEquals("/seerr.jpg", wrapper.stillPath)
    }

    @Test
    fun testStillPathFallsBackWhenSeerrNull() {
        val wrapper = EpisodeWrapper(seerrEpisode = seerrEpisode(stillPath = null))
        assertNull(wrapper.stillPath)
    }

    @Test
    fun testAirDatePrefersArrEpisode() {
        val wrapper = EpisodeWrapper(arrEpisode = arrEpisode(airDate = LocalDate(2021, 6, 1)))
        assertEquals(LocalDate(2021, 6, 1), wrapper.airDate)
    }

    @Test
    fun testMonitoredReflectsArrEpisode() {
        val monitored = EpisodeWrapper(arrEpisode = arrEpisode(monitored = true))
        assertEquals(true, monitored.monitored)
        assertTrue(monitored.isMonitored)

        val unmonitored = EpisodeWrapper(arrEpisode = arrEpisode(monitored = false))
        assertEquals(false, unmonitored.monitored)
        assertFalse(unmonitored.isMonitored)
    }

    @Test
    fun testHasFileMatchesArrEpisode() {
        val withFile = EpisodeWrapper(arrEpisode = arrEpisode(hasFile = true))
        assertTrue(withFile.hasFile)

        val withoutFile = EpisodeWrapper(arrEpisode = arrEpisode(hasFile = false))
        assertFalse(withoutFile.hasFile)
    }

    @Test
    fun testFileQualityNameFromEpisodeFile() {
        val file =
            EpisodeFile(
                id = 1,
                relativePath = "e.mkv",
                size = 100,
                qualityCutoffNotMet = false,
                quality =
                    QualityInfo(
                        quality = Quality(id = 1, name = "1080p"),
                        revision = Revision(version = 1, real = 0, isRepack = false),
                    ),
                seriesId = 7,
                seasonNumber = 1,
            )
        val wrapper = EpisodeWrapper(arrEpisode = arrEpisode(file = file, hasFile = true))
        assertEquals("1080p", wrapper.fileQualityName)
    }

    @Test
    fun testActivityProgressStoredAsProvided() {
        val wrapper = EpisodeWrapper(isActive = true, activityProgress = "42%")
        assertTrue(wrapper.isActive)
        assertEquals("42%", wrapper.activityProgress)
    }
}
