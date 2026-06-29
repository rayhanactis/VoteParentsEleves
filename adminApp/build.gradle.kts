import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

group = "com.rayhanactis"
version = "1.0.0-SNAPSHOT"

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(projects.shared)
    implementation(projects.server)

    implementation(compose.desktop.currentOs)
    implementation(compose.material3)

    implementation(libs.androidx.lifecycle.viewmodelCompose)
    implementation(libs.androidx.lifecycle.runtimeCompose)

    // Fournit Dispatchers.Main au-dessus du Swing EDT (sinon viewModelScope crash).
    implementation(libs.kotlinx.coroutines.swing)

    implementation(ktorLibs.server.core)
    implementation(ktorLibs.server.netty)

    // Génération PDF (fiches identifiants, procès-verbal) + QR codes (fiches, écran de projection).
    implementation(libs.pdfbox)
    implementation(libs.zxing.core)
    implementation(libs.zxing.javase)
}

compose.desktop {
    application {
        mainClass = "com.rayhanactis.voteparentseleves.admin.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Dmg)
            packageName = "VoteParentsEleves-Admin"
            packageVersion = "1.0.0"
            description = "Outil d'administration des élections de parents d'élèves"
            vendor = "VoteParentsEleves"
        }
    }
}
