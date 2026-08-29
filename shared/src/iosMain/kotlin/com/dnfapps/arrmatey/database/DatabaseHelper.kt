package com.dnfapps.arrmatey.database

import com.dnfapps.arrmatey.database.dao.InstanceDao
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DatabaseHelper : KoinComponent {
    private val instanceDao: InstanceDao by inject()

    fun getInstanceDao(): InstanceDao = instanceDao
}
