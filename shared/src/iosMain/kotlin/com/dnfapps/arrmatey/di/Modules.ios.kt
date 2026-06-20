package com.dnfapps.arrmatey.di

import androidx.room.RoomDatabase
import com.dnfapps.arrmatey.database.ArrMateyDatabase
import com.dnfapps.arrmatey.database.getDatabaseBuilder
import com.dnfapps.arrmatey.notifications.NotificationManager
import com.dnfapps.arrmatey.utils.AESEncryptionManager
import com.dnfapps.arrmatey.utils.EncryptionManager
import org.koin.core.module.Module
import org.koin.dsl.module

val iosDbModule = module {
    single<RoomDatabase.Builder<ArrMateyDatabase>> {
        getDatabaseBuilder()
    }
}

val iosSecurityModule = module {
    single<EncryptionManager> { AESEncryptionManager() }
}

val iosNotificationModule = module {
    single { NotificationManager(get()) }
}

actual fun platformModules(): List<Module> = listOf(iosDbModule, iosSecurityModule, iosNotificationModule)
