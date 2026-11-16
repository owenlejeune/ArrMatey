package com.dnfapps.arrmatey.newest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ReleaseNameParserTest {
    @Test
    fun testMovieWithDots() {
        assertEquals(
            ParsedRelease("The Long Walk", 2025),
            ReleaseNameParser.parse("The.Long.Walk.2025.1080p.WEB-DL.DDP5.1.H.264-GRP", ReleaseKind.Movie),
        )
    }

    @Test
    fun testMovieWithSpacesAndParentheses() {
        assertEquals(
            ParsedRelease("Weapons", 2025),
            ReleaseNameParser.parse("Weapons (2025) 2160p WEB-DL", ReleaseKind.Movie),
        )
    }

    @Test
    fun testMovieTitleContainingYearKeepsLastBoundary() {
        assertEquals(
            ParsedRelease("Blade Runner 2049", 2017),
            ReleaseNameParser.parse("Blade.Runner.2049.2017.1080p.BluRay", ReleaseKind.Movie),
        )
    }

    @Test
    fun testMovieLeadingGroupTagIsRemoved() {
        assertEquals(
            ParsedRelease("Sinners", 2025),
            ReleaseNameParser.parse("[GRP] Sinners 2025 1080p", ReleaseKind.Movie),
        )
    }

    @Test
    fun testMovieWithoutYearIsDropped() {
        assertNull(ReleaseNameParser.parse("Some.Movie.1080p.WEB", ReleaseKind.Movie))
    }

    @Test
    fun testMovieStartingWithYearIsDropped() {
        assertNull(ReleaseNameParser.parse("2012.1080p.BluRay", ReleaseKind.Movie))
    }

    @Test
    fun testCamAndTelesyncAreDropped() {
        assertNull(ReleaseNameParser.parse("New.Movie.2025.HDCAM.x264", ReleaseKind.Movie))
        assertNull(ReleaseNameParser.parse("New.Movie.2025.TS.x264", ReleaseKind.Movie))
        assertNull(ReleaseNameParser.parse("New Movie 2025 TELESYNC", ReleaseKind.Movie))
        assertTrue(ReleaseNameParser.isLowQuality("new.movie.2025.camrip"))
    }

    @Test
    fun testTvSeasonAndEpisode() {
        assertEquals(
            ParsedRelease("The Bear", null, 4, 2),
            ReleaseNameParser.parse("The.Bear.S04E02.1080p.WEB.h264-GRP", ReleaseKind.Tv),
        )
    }

    @Test
    fun testTvSeasonPack() {
        assertEquals(
            ParsedRelease("Andor", null, 2, null),
            ReleaseNameParser.parse("Andor.S02.2160p.DSNP.WEB-DL", ReleaseKind.Tv),
        )
    }

    @Test
    fun testTvSeasonWord() {
        assertEquals(
            ParsedRelease("Slow Horses", null, 5, null),
            ReleaseNameParser.parse("Slow Horses Season 5 1080p", ReleaseKind.Tv),
        )
    }

    @Test
    fun testTvWithYear() {
        assertEquals(
            ParsedRelease("Doctor Who", 2005, 1, 1),
            ReleaseNameParser.parse("Doctor.Who.2005.S01E01.720p", ReleaseKind.Tv),
        )
    }

    @Test
    fun testTvWithoutSeasonIsDropped() {
        assertNull(ReleaseNameParser.parse("Some.Show.1080p.WEB", ReleaseKind.Tv))
    }
}
