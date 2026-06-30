package com.rayhanactis.voteparentseleves.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = StatutScrutinSerializer::class)
sealed class StatutScrutin {
    data object Configure : StatutScrutin()
    data object Programme : StatutScrutin()
    data object Ouvert : StatutScrutin()
    data object Ferme : StatutScrutin()
    data object Depouille : StatutScrutin()
}

fun StatutScrutin.code(): String = when (this) {
    StatutScrutin.Configure -> "Configure"
    StatutScrutin.Programme -> "Programme"
    StatutScrutin.Ouvert -> "Ouvert"
    StatutScrutin.Ferme -> "Ferme"
    StatutScrutin.Depouille -> "Depouille"
}

fun statutDepuisCode(code: String): StatutScrutin? = when (code) {
    "Configure" -> StatutScrutin.Configure
    "Programme" -> StatutScrutin.Programme
    "Ouvert" -> StatutScrutin.Ouvert
    "Ferme" -> StatutScrutin.Ferme
    "Depouille" -> StatutScrutin.Depouille
    else -> null
}

object StatutScrutinSerializer : KSerializer<StatutScrutin> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("StatutScrutin", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: StatutScrutin) =
        encoder.encodeString(value.code())

    override fun deserialize(decoder: Decoder): StatutScrutin {
        val raw = decoder.decodeString()
        return statutDepuisCode(raw)
            ?: throw kotlinx.serialization.SerializationException("Statut inconnu: $raw")
    }
}
