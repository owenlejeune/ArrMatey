package com.dnfapps.arrmatey.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<ArrMateyDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("arrmatey_room.db")
    return Room.databaseBuilder<ArrMateyDatabase>(
        context = appContext,
        name = dbFile.absolutePath,
    )
}
