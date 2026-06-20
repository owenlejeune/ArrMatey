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

class Converters: KoinComponent {

    private val json: Json by inject()
    private val encryptionManager: EncryptionManager by inject()

    @TypeConverter
    fun storeEncryptedString(encryptedString: EncryptedString): String {
        return encryptionManager.encrypt(encryptedString.value)
    }

    @TypeConverter
    fun retrieveEncryptedString(value: String): EncryptedString {
        return EncryptedString(encryptionManager.decrypt(value))
    }

    @TypeConverter
    fun storeLanguage(language: Language): String {
        return json.encodeToString(language)
    }

    @TypeConverter
    fun retrieveLanguage(value: String): Language {
        return json.decodeFromString<Language>(value)
    }

    @TypeConverter
    fun storeImage(image: ArrImage): String {
        return json.encodeToString(image)
    }

    @TypeConverter
    fun retrieveImage(value: String): ArrImage {
        return json.decodeFromString<ArrImage>(value)
    }

    @TypeConverter
    fun storeImageList(images: List<ArrImage>): String {
        return json.encodeToString(images)
    }

    @TypeConverter
    fun retrieveImageList(value: String): List<ArrImage> {
        return json.decodeFromString<List<ArrImage>>(value)
    }

    @TypeConverter
    fun storeAlternateTitle(altTitle: AlternateTitle): String {
        return json.encodeToString(altTitle)
    }

    @TypeConverter
    fun retrieveAlternateTitle(value: String): AlternateTitle {
        return json.decodeFromString<AlternateTitle>(value)
    }

    @TypeConverter
    fun storeSeriesAltTitleList(altTitles: List<AlternateTitle>): String {
        return json.encodeToString(altTitles)
    }

    @TypeConverter
    fun retrieveSeriesAltTitleList(value: String): List<AlternateTitle> {
        return json.decodeFromString<List<AlternateTitle>>(value)
    }

    @TypeConverter
    fun storeSeriesAddOptions(addOptions: SeriesAddOptions): String {
        return json.encodeToString(addOptions)
    }

    @TypeConverter
    fun retrieveSeriesAddOptions(value: String): SeriesAddOptions {
        return json.decodeFromString<SeriesAddOptions>(value)
    }

    @TypeConverter
    fun storeSeriesRatings(ratings: SeriesRatings): String {
        return json.encodeToString(ratings)
    }

    @TypeConverter
    fun retrieveSeriesRatings(value: String): SeriesRatings {
        return json.decodeFromString<SeriesRatings>(value)
    }

    @TypeConverter
    fun storeSeriesStats(stats: SeriesStatistics): String {
        return json.encodeToString(stats)
    }

    @TypeConverter
    fun retrieveSeriesState(value: String): SeriesStatistics {
        return json.decodeFromString<SeriesStatistics>(value)
    }

    @TypeConverter
    fun storeSeasons(seasons: List<Season>): String {
        return json.encodeToString(seasons)
    }

    @TypeConverter
    fun retrieveSeasons(value: String): List<Season> {
        return json.decodeFromString<List<Season>>(value)
    }

    @TypeConverter
    fun storeMovieRatings(ratings: MovieRatings): String {
        return json.encodeToString(ratings)
    }

    @TypeConverter
    fun retrieveMovieRatings(value: String): MovieRatings {
        return json.decodeFromString<MovieRatings>(value)
    }

    @TypeConverter
    fun storeMovieStats(stats: MovieStatistics): String {
        return json.encodeToString(stats)
    }

    @TypeConverter
    fun retrieveMovieStats(value: String): MovieStatistics {
        return json.decodeFromString<MovieStatistics>(value)
    }

    @TypeConverter
    fun storeMovieFile(movieFile: MovieFile): String {
        return json.encodeToString(movieFile)
    }

    @TypeConverter
    fun retrieveMovieFile(value: String): MovieFile {
        return json.decodeFromString<MovieFile>(value)
    }

    @TypeConverter
    fun storeMovieCollection(collection: MovieCollection): String {
        return json.encodeToString(collection)
    }

    @TypeConverter
    fun retrieveMovieCollection(value: String): MovieCollection {
        return json.decodeFromString<MovieCollection>(value)
    }

    @TypeConverter
    fun storeStringList(list: List<String>): String {
        return json.encodeToString(list)
    }

    @TypeConverter
    fun retrieveStringList(value: String): List<String> {
        return json.decodeFromString<List<String>>(value)
    }

    @TypeConverter
    fun storeIntList(list: List<Int>): String {
        return json.encodeToString(list)
    }

    @TypeConverter
    fun retrieveIntList(value: String): List<Int> {
        return json.decodeFromString<List<Int>>(value)
    }

    @TypeConverter
    fun storeInstant(instant: Instant) = instant.toEpochMilliseconds()

    @TypeConverter
    fun retrieveInstant(millis: Long) = Instant.fromEpochMilliseconds(millis)

    @TypeConverter
    fun fromHeaderList(headers: List<InstanceHeader>): String {
        return Json.encodeToString(headers)
    }

    @TypeConverter
    fun toHeaderList(headersString: String): List<InstanceHeader> {
        return if (headersString.isEmpty()) {
            emptyList()
        } else {
            try {
                Json.decodeFromString(headersString)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

}