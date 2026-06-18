package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class AntiCaptchaSettings(
    val anti_captcha_key: String
)
