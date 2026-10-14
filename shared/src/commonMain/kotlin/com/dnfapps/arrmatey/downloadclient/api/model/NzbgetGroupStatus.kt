package com.dnfapps.arrmatey.downloadclient.api.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = NzbgetGroupStatusSerializer::class)
enum class NzbgetGroupStatus {
    Queued,
    Paused,
    Downloading,
    ParCheck,
    Repair,
    Unpack,
    PostUnpackRenaming,
    Moving,
    PostProcessingQueued,
    PostProcessingScript,
    QueueScriptQueued,
    QueueScriptExecuting,
    Success,
    Failure,
    Unknown,
    ;

    val isPaused: Boolean
        get() = this == Paused
}

object NzbgetGroupStatusSerializer : KSerializer<NzbgetGroupStatus> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("NzbgetGroupStatus", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: NzbgetGroupStatus) {
        encoder.encodeString(value.name.uppercase())
    }

    override fun deserialize(decoder: Decoder): NzbgetGroupStatus {
        val raw = decoder.decodeString().uppercase()
        return when {
            raw == "QUEUED" -> NzbgetGroupStatus.Queued
            raw == "PAUSED" -> NzbgetGroupStatus.Paused
            raw == "DOWNLOADING" -> NzbgetGroupStatus.Downloading
            raw == "PARCHECK" -> NzbgetGroupStatus.ParCheck
            raw == "REPAIR" -> NzbgetGroupStatus.Repair
            raw == "UNPACK" -> NzbgetGroupStatus.Unpack
            raw == "POST_UNPACK_RENAMING" -> NzbgetGroupStatus.PostUnpackRenaming
            raw == "MOVING" -> NzbgetGroupStatus.Moving
            raw == "PP-QUEUED" || raw == "PP_QUEUED" || raw == "QUEUED/PP" -> NzbgetGroupStatus.PostProcessingQueued
            raw == "PP-SCRIPT" || raw == "PP_SCRIPT" || raw == "POST-PROCESSING" -> NzbgetGroupStatus.PostProcessingScript
            raw == "QS_QUEUED" -> NzbgetGroupStatus.QueueScriptQueued
            raw == "QS_EXECUTING" -> NzbgetGroupStatus.QueueScriptExecuting
            raw.startsWith("SUCCESS") -> NzbgetGroupStatus.Success
            raw.startsWith("FAILURE") || raw.contains("ERROR") -> NzbgetGroupStatus.Failure
            else -> NzbgetGroupStatus.Unknown
        }
    }
}
