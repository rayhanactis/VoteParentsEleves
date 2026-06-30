package com.rayhanactis.voteparentseleves.server.mail

import com.rayhanactis.voteparentseleves.model.ParametresEcole
import jakarta.mail.Authenticator
import jakarta.mail.Message
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import java.util.Properties

sealed class ResultatEmail {
    data object Succes : ResultatEmail()
    data class Echec(val raison: String) : ResultatEmail()
}

object EmailService {

    const val SUJET = "Vos identifiants pour l'élection des parents d'élèves"

    fun smtpConfigure(p: ParametresEcole): Boolean =
        p.smtpHost.isNotBlank() && p.emailExpediteur.isNotBlank()

    fun envoyer(
        config: ParametresEcole,
        destinataire: String,
        sujet: String,
        corps: String
    ): ResultatEmail {
        if (!smtpConfigure(config)) {
            return ResultatEmail.Echec("Configuration SMTP incomplète (voir Paramètres).")
        }
        if (destinataire.isBlank()) {
            return ResultatEmail.Echec("Adresse email du destinataire manquante.")
        }
        return try {
            val props = Properties().apply {
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
                put("mail.smtp.host", config.smtpHost)
                put("mail.smtp.port", config.smtpPort.toString())
            }
            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication =
                    PasswordAuthentication(config.emailExpediteur, config.smtpMotDePasse)
            })
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(config.emailExpediteur))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire))
                subject = sujet
                setText(corps, "UTF-8")
            }
            Transport.send(message)
            ResultatEmail.Succes
        } catch (t: Throwable) {
            ResultatEmail.Echec(t.message ?: "Erreur d'envoi inconnue")
        }
    }

    fun corpsIdentifiants(
        prenom: String,
        nom: String,
        nomEcole: String,
        identifiant: String,
        motDePasse: String
    ): String {
        val ecole = nomEcole.ifBlank { "votre école" }
        return buildString {
            appendLine("Bonjour $prenom $nom,")
            appendLine()
            appendLine("Voici vos identifiants pour l'élection des parents d'élèves de $ecole :")
            appendLine()
            appendLine("Identifiant : $identifiant")
            appendLine("Mot de passe : $motDePasse")
            appendLine()
            appendLine("Le jour du vote, scannez le QR code projeté par l'école avec l'application, puis saisissez ces identifiants.")
            appendLine()
            appendLine("Votre vote est anonyme et sécurisé. Ce message est personnel : ne le transférez pas.")
            appendLine()
            appendLine("Cordialement,")
            append("L'équipe organisatrice")
        }
    }
}
