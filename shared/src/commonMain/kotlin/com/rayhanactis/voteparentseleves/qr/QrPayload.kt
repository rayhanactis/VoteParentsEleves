package com.rayhanactis.voteparentseleves.qr

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

sealed class QrPayload {
    data class DecouverteServeur(val baseUrl: String) : QrPayload()
    data class Identifiants(val code: String, val motDePasse: String, val scrutinId: String) : QrPayload()
    data object Inconnu : QrPayload()
}

@Serializable
internal data class PayloadIdentifiantsBrut(
    val type: String = "",
    val code: String = "",
    val motDePasse: String = "",
    val scrutinId: String = ""
)

private const val TYPE_IDENTIFIANTS = "vpe-identifiants"
private const val PREFIXE_DECOUVERTE = "vpe-serveur:"

private val jsonQr = Json { ignoreUnknownKeys = true; isLenient = true }

fun parserQrPayload(brut: String): QrPayload {
    val contenu = brut.trim()
    if (contenu.startsWith(PREFIXE_DECOUVERTE)) {
        val baseUrl = contenu.removePrefix(PREFIXE_DECOUVERTE).trim()
        return if (baseUrl.isNotBlank()) QrPayload.DecouverteServeur(baseUrl) else QrPayload.Inconnu
    }
    val parse = runCatching { jsonQr.decodeFromString<PayloadIdentifiantsBrut>(contenu) }.getOrNull()
    if (parse != null && parse.type == TYPE_IDENTIFIANTS && parse.code.isNotBlank() && parse.motDePasse.isNotBlank()) {
        return QrPayload.Identifiants(parse.code, parse.motDePasse, parse.scrutinId)
    }
    return QrPayload.Inconnu
}

fun construireQrDecouverte(baseUrl: String): String = "$PREFIXE_DECOUVERTE$baseUrl"

fun construireQrIdentifiants(code: String, motDePasse: String, scrutinId: String): String =
    jsonQr.encodeToString(
        PayloadIdentifiantsBrut(type = TYPE_IDENTIFIANTS, code = code, motDePasse = motDePasse, scrutinId = scrutinId)
    )
