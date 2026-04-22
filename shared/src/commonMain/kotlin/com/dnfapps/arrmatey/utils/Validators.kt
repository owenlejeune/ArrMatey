package com.dnfapps.arrmatey.utils

fun String.isValidUrl(): Boolean {
    // Regular expression pattern for URL validation
    // Matches http(s)://(?:userinfo@)?host(:port)?(/path)?
    // userinfo can contain anything except whitespace and @
    // host can be anything except whitespace, / and :
    // port is 1-5 digits
    // path starts with /
    val urlPattern = Regex(
        "^https?://(?:[^\\s@]+@)?([^\\s/:]+)(?::([0-9]{1,5}))?(?:/.*)?$",
        RegexOption.IGNORE_CASE
    )

    val match = urlPattern.matchEntire(this) ?: return false

    // Extract and validate port if present
    match.groups[2]?.value?.toIntOrNull()?.let { port ->
        // Port must be between 1 and 65535
        if (port !in 1..65535) {
            return false
        }
    }

    return true
}
