package com.rayhanactis.voteparentseleves.admin.pdf

import com.rayhanactis.voteparentseleves.admin.ElecteurGenere
import com.rayhanactis.voteparentseleves.admin.qr.genererQrBufferedImage
import com.rayhanactis.voteparentseleves.admin.qr.versPng
import com.rayhanactis.voteparentseleves.model.ResultatScrutin
import com.rayhanactis.voteparentseleves.qr.construireQrIdentifiants
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import java.io.File
import java.nio.charset.Charset

private val POLICE_TITRE = PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)
private val POLICE_TEXTE = PDType1Font(Standard14Fonts.FontName.HELVETICA)

private val ENCODEUR_WINANSI = Charset.forName("windows-1252").newEncoder()

/**
 * Helvetica standard de PDFBox n'accepte que l'encodage WinAnsi (CP1252). On
 * remplace d'abord la ponctuation typographique par son équivalent ASCII, puis
 * on neutralise tout caractère restant non encodable (→ `?`) pour ne jamais
 * lever `U+XXXX is not available in this font's encoding`.
 */
private fun assainirWinAnsi(contenu: String): String {
    val preTraite = contenu
        .replace('’', '\'').replace('‘', '\'')
        .replace('“', '"').replace('”', '"')
        .replace('–', '-').replace('—', '-')
        .replace(' ', ' ')
        .replace("…", "...")
        .replace("→", "->").replace("←", "<-")
        .replace("✓", "[OK]").replace("✕", "x").replace("✗", "x")
    return buildString {
        preTraite.forEach { c -> append(if (ENCODEUR_WINANSI.canEncode(c)) c else '?') }
    }
}

private fun PDPageContentStream.texte(x: Float, y: Float, taille: Float, police: PDType1Font, contenu: String) {
    beginText()
    setFont(police, taille)
    newLineAtOffset(x, y)
    showText(assainirWinAnsi(contenu))
    endText()
}

fun genererPdfFichesIdentifiants(
    electeurs: List<ElecteurGenere>,
    nomEcole: String,
    destination: File
) {
    PDDocument().use { doc ->
        electeurs.forEach { e ->
            val page = PDPage(PDRectangle.A4)
            doc.addPage(page)
            val payload = construireQrIdentifiants(code = e.id, motDePasse = e.motDePasseClair, scrutinId = "")
            val qrImage = genererQrBufferedImage(payload, 500)
            val pdImage = PDImageXObject.createFromByteArray(doc, qrImage.versPng(), "qr-${e.id}")
            val largeurPage = page.mediaBox.width

            PDPageContentStream(doc, page).use { cs ->
                cs.texte(50f, 770f, 20f, POLICE_TITRE, nomEcole.ifBlank { "Élection des parents d'élèves" })
                cs.texte(50f, 740f, 14f, POLICE_TEXTE, "Vos identifiants de vote — ${e.prenom} ${e.nom}")

                val tailleImg = 260f
                cs.drawImage(pdImage, (largeurPage - tailleImg) / 2, 420f, tailleImg, tailleImg)

                cs.texte(50f, 380f, 16f, POLICE_TITRE, "Code : ${e.id}")
                cs.texte(50f, 352f, 16f, POLICE_TITRE, "Mot de passe : ${e.motDePasseClair}")
                cs.texte(50f, 310f, 11f, POLICE_TEXTE, "Scannez le QR avec l'application de vote, ou saisissez le code")
                cs.texte(50f, 295f, 11f, POLICE_TEXTE, "et le mot de passe ci-dessus dans l'écran de connexion.")
                cs.texte(50f, 270f, 11f, POLICE_TEXTE, "Votre vote est anonyme et sécurisé. Ce document est personnel : ne le partagez pas.")
            }
        }
        doc.save(destination)
    }
}

fun genererPdfProcesVerbal(
    resultat: ResultatScrutin,
    nomScrutin: String,
    destination: File
) {
    PDDocument().use { doc ->
        var page = PDPage(PDRectangle.A4)
        doc.addPage(page)
        var cs = PDPageContentStream(doc, page)
        var y = 770f

        fun ligne(taille: Float, police: PDType1Font, contenu: String) {
            if (y < 60f) {
                cs.close()
                page = PDPage(PDRectangle.A4)
                doc.addPage(page)
                cs = PDPageContentStream(doc, page)
                y = 770f
            }
            cs.texte(50f, y, taille, police, contenu)
            y -= taille + 6f
        }

        ligne(18f, POLICE_TITRE, "Procès-verbal de dépouillement")
        ligne(13f, POLICE_TEXTE, nomScrutin)
        y -= 10f
        resultat.procesVerbal.lines().forEach { l -> ligne(11f, POLICE_TEXTE, l.take(100)) }

        cs.close()
        doc.save(destination)
    }
}
