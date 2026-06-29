package com.rayhanactis.voteparentseleves.admin.reseau

import java.net.Inet4Address
import java.net.NetworkInterface

// Première adresse IPv4 LAN non-loopback trouvée : c'est celle que les
// téléphones des parents doivent utiliser pour joindre le serveur embarqué
// (Wi-Fi local de l'école, cf. ROADMAP — architecture kiosque par école).
fun adresseIpLocale(): String? = runCatching {
    NetworkInterface.getNetworkInterfaces().asSequence()
        .filter { it.isUp && !it.isLoopback && !it.isVirtual }
        .flatMap { it.inetAddresses.asSequence() }
        .filterIsInstance<Inet4Address>()
        .firstOrNull { !it.isLoopbackAddress }
        ?.hostAddress
}.getOrNull()
