package com.dnfapps.arrmatey.database

import androidx.room.TypeConverter
import com.dnfapps.arrmatey.arr.api.model.AlternateTitle
import com.dnfapps.arrmatey.arr.api.model.ArrImage
import com.dnfapps.arrmatey.arr.api.model.Language
import com.dnfapps.arrmatey.arr.api.model.MovieCollection
import com.dnfapps.arrmatey.arr.api.model.MovieFile
import com.dnfapps.arrmatey.arr.api.model.MovieRatings
import com.dnfapps.arrmatey.arr.api.model.MovieStatistics
import com.dnfapps.arrmatey.arr.api.model.Season
import com.dnfapps.arrmatey.arr.api.model.SeriesAddOptions
import com.dnfapps.arrmatey.arr.api.model.SeriesRatings
import com.dnfapps.arrmatey.arr.api.model.SeriesStatistics
import com.dnfapps.arrmatey.instances.model.InstanceHeader
import com.dnfapps.arrmatey.utils.EncryptionManager
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Instant

class Converters : KoinComponent {
    private val json: Json by inject()
    private val encryptionManager: EncryptionManager by inject()

    @TypeConverter
    fun storeEncryptedString(encryptedString: EncryptedString): String = encryptionManager.encrypt(encryptedString.value)

    @TypeConverter
    fun retrieveEncryptedString(value: String): EncryptedString = EncryptedString(encryptionManager.decrypt(value))

    @TypeConverter
    fun storeLanguage(language: Language): String = json.encodeToString(language)

    @TypeConverter
    fun retrieveLanguage(value: String): Language = json.decodeFromString<Language>(value)

    @TypeConverter
    fun storeImage(image: ArrImage): String = json.encodeToString(image)

    @TypeConverter
    fun retrieveImage(value: String): ArrImage = json.decodeFromString<ArrImage>(value)

    @TypeConverter
    fun storeImageList(images: List<ArrImage>): String = json.encodeToString(images)

    @TypeConverter
    fun retrieveImageList(value: String): List<ArrImage> = json.decodeFromString<List<ArrImage>>(value)

    @TypeConverter
    fun storeAlternateTitle(altTitle: AlternateTitle): String = json.encodeToString(altTitle)

    @TypeConverter
    fun retrieveAlternateTitle(value: String): AlternateTitle = json.decodeFromString<AlternateTitle>(value)

    @TypeConverter
    fun storeSeriesAltTitleList(altTitles: List<AlternateTitle>): String = json.encodeToString(altTitles)

    @TypeConverter
    fun retrieveSeriesAltTitleList(value: String): List<AlternateTitle> = json.decodeFromString<List<AlternateTitle>>(value)

    @TypeConverter
    fun storeSeriesAddOptions(addOptions: SeriesAddOptions): String = json.encodeToString(addOptions)

    @TypeConverter
    fun retrieveSeriesAddOptions(value: String): SeriesAddOptions = json.decodeFromString<SeriesAddOptions>(value)

    @TypeConverter
    fun storeSeriesRatings(ratings: SeriesRatings): String = json.encodeToString(ratings)

    @TypeConverter
    fun retrieveSeriesRatings(value: String): SeriesRatings = json.decodeFromString<SeriesRatings>(value)

    @TypeConverter
    fun storeSeriesStats(stats: SeriesStatistics): String = json.encodeToString(stats)

    @TypeConverter
    fun retrieveSeriesState(value: String): SeriesStatistics = json.decodeFromString<SeriesStatistics>(value)

    @TypeConverter
    fun storeSeasons(seasons: List<Season>): String = json.encodeToString(seasons)

    @TypeConverter
    fun retrieveSeasons(value: String): List<Season> = json.decodeFromString<List<Season>>(value)

    @TypeConverter
    fun storeMovieRatings(ratings: MovieRatings): String = json.encodeToString(ratings)

    @TypeConverter
    fun retrieveMovieRatings(value: String): MovieRatings = json.decodeFromString<MovieRatings>(value)

    @TypeConverter
    fun storeMovieStats(stats: MovieStatistics): String = json.encodeToString(stats)

    @TypeConverter
    fun retrieveMovieStats(value: String): MovieStatistics = json.decodeFromString<MovieStatistics>(value)

    @TypeConverter
    fun storeMovieFile(movieFile: MovieFile): String = json.encodeToString(movieFile)

    @TypeConverter
    fun retrieveMovieFile(value: String): MovieFile = json.decodeFromString<MovieFile>(value)

    @TypeConverter
    fun storeMovieCollection(collection: MovieCollection): String = json.encodeToString(collection)

    @TypeConverter
    fun retrieveMovieCollection(value: String): MovieCollection = json.decodeFromString<MovieCollection>(value)

    @TypeConverter
    fun storeStringList(list: List<String>): String = json.encodeToString(list)

    @TypeConverter
    fun retrieveStringList(value: String): List<String> = json.decodeFromString<List<String>>(value)

    @TypeConverter
    fun storeIntList(list: List<Int>): String = json.encodeToString(list)

    @TypeConverter
    fun retrieveIntList(value: String): List<Int> = json.decodeFromString<List<Int>>(value)

    @TypeConverter
    fun storeInstant(instant: Instant) = instant.toEpochMilliseconds()

    @TypeConverter
    fun retrieveInstant(millis: Long) = Instant.fromEpochMilliseconds(millis)

    @TypeConverter
    fun fromHeaderList(headers: List<InstanceHeader>): String = Json.encodeToString(headers)

    @TypeConverter
    fun toHeaderList(headersString: String): List<InstanceHeader> =
        if (headersString.isEmpty()) {
            emptyList()
        } else {
            try {
                Json.decodeFromString(headersString)
            } catch (e: Exception) {
                emptyList()
            }
        }
}
