package com.dnfapps.arrmatey.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.dnfapps.arrmatey.database.dao.InstanceDao
import com.dnfapps.arrmatey.database.migrations.migrations
import com.dnfapps.arrmatey.downloadclient.database.DownloadClientDao
import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import com.dnfapps.arrmatey.instances.model.Instance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(
    entities = [Instance::class, DownloadClient::class],
    version = 5,
    exportSchema = true
)
@TypeConverters(Converters::class)
@ConstructedBy(ArrMateyDatabaseConstructor::class)
abstract class ArrMateyDatabase : RoomDatabase() {
    abstract fun getInstanceDao(): InstanceDao
    abstract fun getDownloadClientDao(): DownloadClientDao
}

@Suppress("KotlinNoActualForExpect")
expect object ArrMateyDatabaseConstructor : RoomDatabaseConstructor<ArrMateyDatabase> {
    override fun initialize(): ArrMateyDatabase
}

fun getRoomDatabase(
    builder: RoomDatabase.Builder<ArrMateyDatabase>
): ArrMateyDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .addMigrations(migrations = migrations.toTypedArray())
        .build()
}
