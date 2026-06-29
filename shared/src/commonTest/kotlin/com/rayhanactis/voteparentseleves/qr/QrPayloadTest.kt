package com.rayhanactis.voteparentseleves.qr

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class QrPayloadTest {

    @Test
    fun `construit puis reparse un QR de decouverte serveur`() {
        val brut = construireQrDecouverte("http://192.168.1.42:8080")
        val payload = assertIs<QrPayload.DecouverteServeur>(parserQrPayload(brut))
        assertEquals("http://192.168.1.42:8080", payload.baseUrl)
    }

    @Test
    fun `construit puis reparse un QR d'identifiants electeur`() {
        val brut = construireQrIdentifiants(code = "alice.dupont", motDePasse = "384021", scrutinId = "scr-1")
        val payload = assertIs<QrPayload.Identifiants>(parserQrPayload(brut))
        assertEquals("alice.dupont", payload.code)
        assertEquals("384021", payload.motDePasse)
        assertEquals("scr-1", payload.scrutinId)
    }

    @Test
    fun `un contenu quelconque scanne par erreur est marque Inconnu`() {
        assertEquals(QrPayload.Inconnu, parserQrPayload("https://example.com"))
        assertEquals(QrPayload.Inconnu, parserQrPayload(""))
        assertEquals(QrPayload.Inconnu, parserQrPayload("{\"type\":\"autre-chose\"}"))
    }

    @Test
    fun `un prefixe de decouverte sans url est Inconnu`() {
        assertEquals(QrPayload.Inconnu, parserQrPayload("vpe-serveur:"))
    }
}
