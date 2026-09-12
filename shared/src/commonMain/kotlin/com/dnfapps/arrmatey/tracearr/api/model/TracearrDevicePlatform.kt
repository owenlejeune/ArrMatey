package com.dnfapps.arrmatey.tracearr.api.model

enum class TracearrDevicePlatform {
    PHONE,
    TABLET,
    TV,
    DESKTOP,
    CONSOLE,
    UNKNOWN,
    ;

    companion object {
        fun fromSession(
            platform: String?,
            product: String? = null,
            device: String? = null,
        ): TracearrDevicePlatform {
            val rawTokens =
                listOfNotNull(platform, product, device)
                    .joinToString(" ")
                    .lowercase()

            if (rawTokens.isBlank()) return UNKNOWN

            return when {
                // Check TV / Set-top boxes first (to avoid 'android tv' falling into mobile android)
                rawTokens.containsAny(
                    "tv",
                    "appletv",
                    "apple tv",
                    "roku",
                    "firetv",
                    "fire tv",
                    "chromecast",
                    "cast",
                    "tizen",
                    "webos",
                    "shield",
                    "bravia",
                ) -> TV

                // Consoles
                rawTokens.containsAny(
                    "playstation",
                    "ps4",
                    "ps5",
                    "xbox",
                ) -> CONSOLE

                // Tablets & Phones
                rawTokens.containsAny("ipad", "tablet") -> TABLET
                rawTokens.containsAny(
                    "iphone",
                    "android",
                    "mobile",
                    "phone",
                    "ios",
                ) -> PHONE

                // Desktop / Web browsers
                rawTokens.containsAny(
                    "windows",
                    "mac",
                    "macos",
                    "osx",
                    "linux",
                    "chrome",
                    "firefox",
                    "safari",
                    "edge",
                    "opera",
                    "web",
                ) -> DESKTOP

                else -> UNKNOWN
            }
        }

        private fun String.containsAny(vararg patterns: String): Boolean = patterns.any { this.contains(it) }
    }
}
