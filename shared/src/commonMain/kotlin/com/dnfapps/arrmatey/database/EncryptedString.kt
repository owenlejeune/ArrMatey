package com.dnfapps.arrmatey.database

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class EncryptedString(val value: String)
