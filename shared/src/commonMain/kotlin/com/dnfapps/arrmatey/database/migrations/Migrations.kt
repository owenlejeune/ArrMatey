package com.dnfapps.arrmatey.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

private val MIGRATION_1_2 = object: Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "ALTER TABLE instances ADD COLUMN headers TEXT NOT NULL DEFAULT '[]'"
        )
    }
}

private val MIGRATION_2_3 = object: Migration(2, 3) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            """
            ALTER TABLE instances 
            ADD COLUMN localNetworkEnabled INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
        )

        connection.execSQL(
            """
            ALTER TABLE instances 
            ADD COLUMN localNetworkEndpoint TEXT DEFAULT NULL
            """.trimIndent()
        )

        connection.execSQL(
            """
            ALTER TABLE instances 
            ADD COLUMN localNetworkSsid TEXT DEFAULT NULL
            """.trimIndent()
        )
    }
}

private val MIGRATION_3_4 = object: Migration(3, 4) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `download_clients` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `type` TEXT NOT NULL,
                `label` TEXT NOT NULL,
                `url` TEXT NOT NULL,
                `username` TEXT NOT NULL DEFAULT '',
                `password` TEXT NOT NULL DEFAULT '',
                `apiKey` TEXT NOT NULL DEFAULT '',
                `enabled` INTEGER NOT NULL,
                `selected` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        connection.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_download_clients_url` ON `download_clients` (`url`)"
        )
        connection.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_download_clients_label` ON `download_clients` (`label`)"
        )
    }
}

private val MIGRATION_4_5 = object: Migration(4, 5) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "ALTER TABLE download_clients ADD COLUMN headers TEXT NOT NULL DEFAULT '[]'"
        )
    }
}

private val MIGRATION_5_6 = object: Migration(5, 6) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("""
                CREATE TABLE IF NOT EXISTS custom_webpages (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    url TEXT NOT NULL,
                    headers TEXT NOT NULL DEFAULT '[]'
                )
            """.trimIndent()
        )
    }
}

private val MIGRATION_6_7 = object: Migration(6, 7) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("""
            ALTER TABLE instances ADD COLUMN notificationsEnabled INTEGER NOT NULL DEFAULT 0
        """.trimIndent())
    }
}

val migrations = arrayOf(
    MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7
)
