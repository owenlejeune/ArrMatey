package com.dnfapps.arrmatey.utils

import com.dnfapps.arrmatey.monitoring.model.PlaybackMediaType
import com.dnfapps.arrmatey.tautulli.api.model.TautulliMediaType
import com.dnfapps.arrmatey.tracearr.api.model.TracearrMediaType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AspectRatioTest {
    @Test
    fun testPosterRatio() {
        assertEquals(0.675f, AspectRatio.Poster.ratio)
    }

    @Test
    fun testCoverRatio() {
        assertEquals(1f, AspectRatio.Cover.ratio)
    }

    @Test
    fun testAllValuesAccountedFor() {
        assertEquals(setOf(AspectRatio.Poster, AspectRatio.Cover), AspectRatio.entries.toSet())
    }

    @Test
    fun testPlaybackMediaTypeAspectRatio() {
        assertTrue(PlaybackMediaType.Track.isAudio)
        assertEquals(AspectRatio.Cover, PlaybackMediaType.Track.aspectRatio)

        assertFalse(PlaybackMediaType.Movie.isAudio)
        assertEquals(AspectRatio.Poster, PlaybackMediaType.Movie.aspectRatio)

        assertFalse(PlaybackMediaType.Episode.isAudio)
        assertEquals(AspectRatio.Poster, PlaybackMediaType.Episode.aspectRatio)
    }

    @Test
    fun testTautulliMediaTypeAspectRatio() {
        assertTrue(TautulliMediaType.Artist.isAudio)
        assertTrue(TautulliMediaType.Album.isAudio)
        assertTrue(TautulliMediaType.Track.isAudio)

        assertEquals(AspectRatio.Cover, TautulliMediaType.Artist.aspectRatio)
        assertEquals(AspectRatio.Cover, TautulliMediaType.Album.aspectRatio)
        assertEquals(AspectRatio.Cover, TautulliMediaType.Track.aspectRatio)

        assertFalse(TautulliMediaType.Movie.isAudio)
        assertEquals(AspectRatio.Poster, TautulliMediaType.Movie.aspectRatio)

        assertFalse(TautulliMediaType.Episode.isAudio)
        assertEquals(AspectRatio.Poster, TautulliMediaType.Episode.aspectRatio)
    }

    @Test
    fun testTracearrMediaTypeAspectRatio() {
        assertTrue(TracearrMediaType.Track.isAudio)
        assertEquals(AspectRatio.Cover, TracearrMediaType.Track.aspectRatio)

        assertFalse(TracearrMediaType.Movie.isAudio)
        assertEquals(AspectRatio.Poster, TracearrMediaType.Movie.aspectRatio)

        assertFalse(TracearrMediaType.Episode.isAudio)
        assertEquals(AspectRatio.Poster, TracearrMediaType.Episode.aspectRatio)
    }
}
