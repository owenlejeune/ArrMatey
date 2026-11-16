package com.dnfapps.arrmatey.newest

/**
 * A release name reduced to what is needed to look the title up.
 * [season] and [episode] are only set for TV releases.
 */
data class ParsedRelease(
    val title: String,
    val year: Int?,
    val season: Int? = null,
    val episode: Int? = null
)

enum class ReleaseKind { Movie, Tv }

/**
 * Turns an indexer release name such as `Some.Movie.2024.1080p.WEB-DL.x264-GRP`
 * into a title, year and season. Returns null for names that cannot be matched,
 * and for low-quality sources (CAM, TS, screeners and similar).
 */
object ReleaseNameParser {

    private val lowQuality = Regex(
        """\b(CAM|HDCAM|CAMRIP|TS|HDTS|TELESYNC|TELECINE|TC|HC|HARDCODED|SCR|SCREENER|DVDSCR|WORKPRINT)\b""",
        RegexOption.IGNORE_CASE
    )
    private val year = Regex("""[ .(\[_]((?:19|20)\d\d)(?=[ .)\]_-]|$)""")
    private val tv = Regex(
        """[ .]S(\d{1,2})(?:E(\d{1,3}))?(?=[ .]|$)|[ .]Season[ .]?(\d{1,2})""",
        RegexOption.IGNORE_CASE
    )
    private val leadingTag = Regex("""^\[[^\]]*]\s*""")
    private val separators = Regex("""[._]+""")
    private val spaces = Regex("""\s+""")

    fun isLowQuality(name: String): Boolean = lowQuality.containsMatchIn(name)

    fun parse(name: String, kind: ReleaseKind): ParsedRelease? {
        if (isLowQuality(name)) return null
        val stripped = leadingTag.replace(name, "")
        return when (kind) {
            ReleaseKind.Tv -> parseTv(stripped)
            ReleaseKind.Movie -> parseMovie(stripped)
        }
    }

    private fun parseTv(name: String): ParsedRelease? {
        val match = tv.find(name) ?: return null
        var title = clean(name.substring(0, match.range.first))
        val yearMatch = year.find("$title ")
        val releaseYear = yearMatch?.groupValues?.get(1)?.toInt()
        if (yearMatch != null) {
            title = clean(title.substring(0, yearMatch.range.first))
        }
        if (title.isEmpty()) return null
        val season = match.groupValues[1].ifEmpty { match.groupValues[3] }.toIntOrNull()
        val episode = match.groupValues[2].toIntOrNull()
        return ParsedRelease(title, releaseYear, season, episode)
    }

    private fun parseMovie(name: String): ParsedRelease? {
        // The last year wins, so a year inside the title ("Blade Runner 2049") is kept.
        val match = year.findAll(name).lastOrNull() ?: return null
        if (match.range.first == 0) return null
        val title = clean(name.substring(0, match.range.first))
        if (title.isEmpty()) return null
        return ParsedRelease(title, match.groupValues[1].toInt())
    }

    private fun clean(value: String): String =
        spaces.replace(separators.replace(value, " "), " ").trim(' ', '-')
}
