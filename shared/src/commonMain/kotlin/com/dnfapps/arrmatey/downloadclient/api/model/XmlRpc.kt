package com.dnfapps.arrmatey.downloadclient.api.model

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
sealed class XmlRpcValue {
    data class StringValue(val value: String) : XmlRpcValue()
    data class IntValue(val value: Long) : XmlRpcValue()
    data class BooleanValue(val value: Boolean) : XmlRpcValue()
    data class DoubleValue(val value: Double) : XmlRpcValue()
    data class ArrayValue(val values: List<XmlRpcValue>) : XmlRpcValue()
    data class StructValue(val members: Map<String, XmlRpcValue>) : XmlRpcValue()
    data class Base64Value(val bytes: ByteArray) : XmlRpcValue()
    data object NullValue : XmlRpcValue()

    fun asString(): String = when (this) {
        is StringValue -> value
        is IntValue -> value.toString()
        is BooleanValue -> value.toString()
        is DoubleValue -> value.toString()
        else -> ""
    }

    fun asLong(): Long = when (this) {
        is IntValue -> value
        is StringValue -> value.toLongOrNull() ?: 0L
        is DoubleValue -> value.toLong()
        is BooleanValue -> if (value) 1L else 0L
        else -> 0L
    }

    fun asInt(): Int = asLong().toInt()

    fun asBoolean(): Boolean = when (this) {
        is BooleanValue -> value
        is IntValue -> value != 0L
        is StringValue -> value == "1" || value.equals("true", ignoreCase = true)
        else -> false
    }

    fun asList(): List<XmlRpcValue> = when (this) {
        is ArrayValue -> values
        else -> emptyList()
    }

    fun asMap(): Map<String, XmlRpcValue> = when (this) {
        is StructValue -> members
        else -> emptyMap()
    }
}

class XmlRpcFault(
    val faultCode: Int,
    val faultString: String,
) : Exception("XML-RPC Fault ($faultCode): $faultString")

object XmlRpc {
    fun buildCall(method: String, params: List<Any?> = emptyList()): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
        sb.append("<methodCall>")
        sb.append("<methodName>").append(escapeXml(method)).append("</methodName>")
        sb.append("<params>")
        for (p in params) {
            sb.append("<param>")
            appendValue(sb, p)
            sb.append("</param>")
        }
        sb.append("</params>")
        sb.append("</methodCall>")
        return sb.toString()
    }

    private fun appendValue(sb: StringBuilder, v: Any?) {
        sb.append("<value>")
        when (v) {
            null -> sb.append("<string></string>")
            is String -> sb.append("<string>").append(escapeXml(v)).append("</string>")
            is Int -> sb.append("<i4>").append(v).append("</i4>")
            is Long -> sb.append("<i8>").append(v).append("</i8>")
            is Boolean -> sb.append("<boolean>").append(if (v) 1 else 0).append("</boolean>")
            is Double -> sb.append("<double>").append(v).append("</double>")
            is Float -> sb.append("<double>").append(v).append("</double>")
            is ByteArray -> sb.append("<base64>").append(Base64.encode(v)).append("</base64>")
            is List<*> -> {
                sb.append("<array><data>")
                for (item in v) {
                    appendValue(sb, item)
                }
                sb.append("</data></array>")
            }
            is Map<*, *> -> {
                sb.append("<struct>")
                for ((key, value) in v) {
                    sb.append("<member>")
                    sb.append("<name>").append(escapeXml(key.toString())).append("</name>")
                    appendValue(sb, value)
                    sb.append("</member>")
                }
                sb.append("</struct>")
            }
            else -> sb.append("<string>").append(escapeXml(v.toString())).append("</string>")
        }
        sb.append("</value>")
    }

    fun parseResponse(xml: String): XmlRpcValue {
        val tokens = tokenizeXml(xml)
        val reader = TokenReader(tokens)
        var foundMethodResponse = false

        while (reader.hasNext()) {
            val token = reader.next()
            if (token is XmlToken.StartTag) {
                if (token.name == "methodResponse") {
                    foundMethodResponse = true
                }
                when (token.name) {
                    "fault" -> {
                        val value = parseValue(reader)
                        val struct = value.asMap()
                        val code = struct["faultCode"]?.asInt() ?: -1
                        val message = struct["faultString"]?.asString() ?: "Unknown XML-RPC fault"
                        throw XmlRpcFault(code, message)
                    }
                    "param" -> {
                        return parseValue(reader)
                    }
                }
            }
        }
        if (!foundMethodResponse) {
            throw IllegalArgumentException("no methodResponse element")
        }
        return XmlRpcValue.NullValue
    }

    private fun parseValue(reader: TokenReader): XmlRpcValue {
        if (!reader.hasNext()) return XmlRpcValue.NullValue

        when (val next = reader.next()) {
            is XmlToken.EmptyTag -> {
                return if (next.name == "value") XmlRpcValue.StringValue("") else XmlRpcValue.NullValue
            }
            is XmlToken.StartTag -> {
                if (next.name != "value") return XmlRpcValue.NullValue
            }
            is XmlToken.Text -> return XmlRpcValue.StringValue(next.text)
            is XmlToken.EndTag -> return XmlRpcValue.NullValue
        }

        // Inside <value>
        if (!reader.hasNext()) return XmlRpcValue.NullValue

        val peek = reader.peek()
        if (peek is XmlToken.EndTag && peek.name == "value") {
            reader.next()
            return XmlRpcValue.StringValue("")
        }

        val result: XmlRpcValue = when (val token = reader.next()) {
            is XmlToken.Text -> {
                XmlRpcValue.StringValue(token.text)
            }
            is XmlToken.EmptyTag -> {
                when (token.name) {
                    "string" -> XmlRpcValue.StringValue("")
                    "array" -> XmlRpcValue.ArrayValue(emptyList())
                    "struct" -> XmlRpcValue.StructValue(emptyMap())
                    else -> XmlRpcValue.StringValue("")
                }
            }
            is XmlToken.StartTag -> {
                when (token.name) {
                    "string" -> {
                        val text = if (reader.peek() is XmlToken.Text) (reader.next() as XmlToken.Text).text else ""
                        reader.consumeEndTag("string")
                        XmlRpcValue.StringValue(text)
                    }
                    "int", "i4", "i8" -> {
                        val text = if (reader.peek() is XmlToken.Text) (reader.next() as XmlToken.Text).text else "0"
                        reader.consumeEndTag(token.name)
                        XmlRpcValue.IntValue(text.toLongOrNull() ?: 0L)
                    }
                    "boolean" -> {
                        val text = if (reader.peek() is XmlToken.Text) (reader.next() as XmlToken.Text).text else "0"
                        reader.consumeEndTag("boolean")
                        XmlRpcValue.BooleanValue(text == "1" || text.equals("true", ignoreCase = true))
                    }
                    "double" -> {
                        val text = if (reader.peek() is XmlToken.Text) (reader.next() as XmlToken.Text).text else "0.0"
                        reader.consumeEndTag("double")
                        XmlRpcValue.DoubleValue(text.toDoubleOrNull() ?: 0.0)
                    }
                    "base64" -> {
                        val text = if (reader.peek() is XmlToken.Text) (reader.next() as XmlToken.Text).text else ""
                        reader.consumeEndTag("base64")
                        XmlRpcValue.Base64Value(Base64.decode(text.trim()))
                    }
                    "array" -> {
                        val items = mutableListOf<XmlRpcValue>()
                        if (reader.peek() is XmlToken.StartTag && (reader.peek() as XmlToken.StartTag).name == "data") {
                            reader.next() // consume <data>
                            while (reader.hasNext() && reader.peek() != XmlToken.EndTag("data")) {
                                items.add(parseValue(reader))
                            }
                            reader.consumeEndTag("data")
                        } else if (reader.peek() is XmlToken.EmptyTag && (reader.peek() as XmlToken.EmptyTag).name == "data") {
                            reader.next() // consume <data/>
                        }
                        reader.consumeEndTag("array")
                        XmlRpcValue.ArrayValue(items)
                    }
                    "struct" -> {
                        val members = mutableMapOf<String, XmlRpcValue>()
                        while (reader.hasNext() && reader.peek() != XmlToken.EndTag("struct")) {
                            if (reader.peek() is XmlToken.StartTag && (reader.peek() as XmlToken.StartTag).name == "member") {
                                reader.next() // consume <member>
                                reader.consumeStartTag("name")
                                val name = if (reader.peek() is XmlToken.Text) (reader.next() as XmlToken.Text).text else ""
                                reader.consumeEndTag("name")
                                val valObj = parseValue(reader)
                                reader.consumeEndTag("member")
                                members[name] = valObj
                            } else {
                                reader.next()
                            }
                        }
                        reader.consumeEndTag("struct")
                        XmlRpcValue.StructValue(members)
                    }
                    else -> XmlRpcValue.NullValue
                }
            }
            else -> XmlRpcValue.NullValue
        }

        reader.consumeEndTag("value")
        return result
    }

    private fun TokenReader.consumeStartTag(name: String) {
        if (hasNext() && peek() is XmlToken.StartTag && (peek() as XmlToken.StartTag).name == name) {
            next()
        }
    }

    private fun TokenReader.consumeEndTag(name: String) {
        if (hasNext() && peek() is XmlToken.EndTag && (peek() as XmlToken.EndTag).name == name) {
            next()
        }
    }

    private fun escapeXml(s: String): String = buildString(s.length) {
        for (c in s) {
            when (c) {
                '<' -> append("&lt;")
                '>' -> append("&gt;")
                '&' -> append("&amp;")
                '"' -> append("&quot;")
                '\'' -> append("&apos;")
                else -> append(c)
            }
        }
    }

    private fun unescapeXml(s: String): String {
        if (!s.contains('&')) return s
        return s.replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&amp;", "&")
    }

    private fun tokenizeXml(xml: String): List<XmlToken> {
        val tokens = mutableListOf<XmlToken>()
        var i = 0
        val len = xml.length

        while (i < len) {
            if (xml[i] == '<') {
                if (i + 1 < len && xml[i + 1] == '?') {
                    val end = xml.indexOf("?>", i + 2)
                    i = if (end != -1) end + 2 else len
                    continue
                }
                if (i + 3 < len && xml.startsWith("<!--", i)) {
                    val end = xml.indexOf("-->", i + 4)
                    i = if (end != -1) end + 3 else len
                    continue
                }

                val tagEnd = xml.indexOf('>', i)
                if (tagEnd == -1) break
                val rawTag = xml.substring(i + 1, tagEnd).trim()
                i = tagEnd + 1

                if (rawTag.startsWith('/')) {
                    val tagName = rawTag.substring(1).trim().substringBefore(' ')
                    tokens.add(XmlToken.EndTag(tagName))
                } else if (rawTag.endsWith('/')) {
                    val tagName = rawTag.substring(0, rawTag.length - 1).trim().substringBefore(' ')
                    tokens.add(XmlToken.EmptyTag(tagName))
                } else {
                    val tagName = rawTag.substringBefore(' ')
                    tokens.add(XmlToken.StartTag(tagName))
                }
            } else {
                val nextLt = xml.indexOf('<', i)
                val rawText = if (nextLt == -1) {
                    xml.substring(i).also { i = len }
                } else {
                    xml.substring(i, nextLt).also { i = nextLt }
                }
                val text = unescapeXml(rawText.trim())
                if (text.isNotEmpty()) {
                    tokens.add(XmlToken.Text(text))
                }
            }
        }
        return tokens
    }

    private sealed class XmlToken {
        data class StartTag(val name: String) : XmlToken()
        data class EndTag(val name: String) : XmlToken()
        data class EmptyTag(val name: String) : XmlToken()
        data class Text(val text: String) : XmlToken()
    }

    private class TokenReader(private val tokens: List<XmlToken>) {
        private var index = 0

        fun hasNext(): Boolean = index < tokens.size
        fun peek(): XmlToken? = if (index < tokens.size) tokens[index] else null
        fun next(): XmlToken = tokens[index++]
    }
}
