package com.dnfapps.arrmatey.di

import androidx.room.RoomDatabase
import com.dnfapps.arrmatey.database.ArrMateyDatabase
import com.dnfapps.arrmatey.database.getDatabaseBuilder
import com.dnfapps.arrmatey.notifications.NotificationManager
import com.dnfapps.arrmatey.utils.AESEncryptionManager
import com.dnfapps.arrmatey.utils.EncryptionManager
import org.koin.dsl.module

val androidDbModule = module {
    single<RoomDatabase.Builder<ArrMateyDatabase>> {
        getDatabaseBuilder(get())
    }
}

val androidNotificationModule = module {
    single { NotificationManager(get(), get()) }
}

val androidSecurityModule = module {
    single<EncryptionManager> { AESEncryptionManager() }
}

actual fun platformModules() = listOf(androidDbModule, androidSecurityModule, androidNotificationModule)