package com.dnfapps.arrmatey.arr.api.client.logging

private val SENSITIVE_QUERY_PARAM_REGEX =
    Regex("(?i)([?&])(apikey|api_key|apiKey|token|password|pass|secret|auth)=([^&\\s\"'\\n\\r]*)")

private val URL_CREDENTIALS_REGEX =
    Regex("(?i)(https?://)([^:\\s/]+):([^@\\s/]+)@")

private val SENSITIVE_HEADER_REGEX =
    Regex("(?i)^(\\s*(?:->|<-)?\\s*(?:X-Api-Key|Authorization|Cookie|Set-Cookie|[A-Za-z0-9_-]*(?:api[-_]?key|authorization|token|secret|session)[A-Za-z0-9_-]*)\\s*:\\s*).*$", RegexOption.MULTILINE)

private val SENSITIVE_JSON_REGEX =
    Regex("(?i)(\"(?:apikey|api_key|apiKey|token|password|pass|secret|auth)\"\\s*:\\s*)\"[^\"]*\"")

internal fun sanitizeLogMessage(message: String): String {
    if (message.isEmpty()) return message

    return message
        .replace(SENSITIVE_QUERY_PARAM_REGEX, "$1$2=REDACTED")
        .replace(URL_CREDENTIALS_REGEX, "$1$2:REDACTED@")
        .replace(SENSITIVE_HEADER_REGEX, "$1REDACTED")
        .replace(SENSITIVE_JSON_REGEX, "$1\"REDACTED\"")
}

internal fun isExceptionMessage(message: String): Boolean = message.contains("failed with exception", ignoreCase = true)

internal fun isBodyLine(line: String): Boolean {
    val trimmed = line.trim()
    return trimmed.startsWith("BODY") ||
        trimmed.startsWith("{") ||
        trimmed.startsWith("}") ||
        trimmed.startsWith("[") ||
        trimmed.startsWith("]") ||
        trimmed.startsWith("\"")
}
