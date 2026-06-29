package com.rayhanactis.voteparentseleves.admin.qr

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

fun genererQrBufferedImage(contenu: String, taillePx: Int = 360): BufferedImage {
    val hints = mapOf(
        EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
        EncodeHintType.MARGIN to 1
    )
    val matrice = QRCodeWriter().encode(contenu, BarcodeFormat.QR_CODE, taillePx, taillePx, hints)
    val image = BufferedImage(taillePx, taillePx, BufferedImage.TYPE_INT_RGB)
    for (x in 0 until taillePx) {
        for (y in 0 until taillePx) {
            image.setRGB(x, y, if (matrice.get(x, y)) 0x000000 else 0xFFFFFF)
        }
    }
    return image
}

fun BufferedImage.versPng(): ByteArray {
    val out = ByteArrayOutputStream()
    ImageIO.write(this, "png", out)
    return out.toByteArray()
}

fun genererQrImageBitmap(contenu: String, taillePx: Int = 360): ImageBitmap =
    genererQrBufferedImage(contenu, taillePx).toComposeImageBitmap()
