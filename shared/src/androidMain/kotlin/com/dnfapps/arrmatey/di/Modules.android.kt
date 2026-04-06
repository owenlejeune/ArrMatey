package com.dnfapps.arrmatey.di

import androidx.room.RoomDatabase
import com.dnfapps.arrmatey.database.ArrMateyDatabase
import com.dnfapps.arrmatey.database.getDatabaseBuilder
import com.dnfapps.arrmatey.notifications.NotificationManager
import org.koin.dsl.module

val androidDbModule = module {
    single<RoomDatabase.Builder<ArrMateyDatabase>> {
        getDatabaseBuilder(get())
    }
}

val androidNotificationModule = module {
    single { NotificationManager(get(), get()) }
}

actual fun platformModules() = listOf(androidDbModule, androidNotificationModule)