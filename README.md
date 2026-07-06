# VoteParentsEleves

**VoteParentsEleves** est une solution complète de vote électronique pour les élections des représentants de parents d'élèves. Ce projet utilise **Kotlin Multiplatform** et **Compose Multiplatform** pour offrir une expérience fluide et cohérente sur Android, iOS, le Web et le Bureau.

---

## Fonctionnalités

### Pour les Électeurs
*   **Vote Multi-support** : Votez depuis votre smartphone (Android/iOS) ou votre navigateur web.
*   **Sécurité** : Authentification par code unique et mot de passe.
*   **Accessibilité** : Interface simple, moderne et intuitive conçue avec Compose.
*   **Reçu de Vote** : Obtention d'un identifiant de bulletin après chaque vote pour garantir la transparence.

### Pour les Administrateurs (App Desktop)
*   **Gestion des Scrutins** : Création, programmation, ouverture et fermeture des votes.
*   **Import/Export** : Importation de listes d'électeurs et de parents depuis des fichiers.
*   **Logistique** : Génération automatique de fiches d'identifiants (PDF) avec QR codes pour faciliter la connexion.
*   **Résultats instantanés** : Calcul automatique des sièges selon la méthode de la plus forte moyenne (Algorithme de Hare-Niemeyer).
*   **Procès-Verbal** : Génération du PV de fin de scrutin.
*   **Assistant IA** : Aide intégrée via **LangChain4j** et **Ollama** pour assister le jury (branche feature/ia).

---

## Architecture du Projet

Le projet est organisé en plusieurs modules pour maximiser le partage de code :

*   [`/shared`](./shared) : Contient la logique métier partagée, les modèles de données et les composants UI (Compose Multiplatform).
*   [`/server`](./server) : Backend **Ktor** gérant l'API, l'authentification JWT, et la base de données (SQLite avec **Exposed**).
*   [`/adminApp`](./adminApp) : Application Bureau (JVM) riche pour l'administration et la gestion physique du scrutin.
*   [`/webApp`](./webApp) : Client web ciblant **WebAssembly (Wasm)** et **JavaScript**.
*   [`/androidApp`](./androidApp) : Application Android native.
*   [`/iosApp`](./iosApp) : Application iOS (SwiftUI point d'entrée).

---

## Technologies

*   **Langage** : Kotlin (100%)
*   **UI** : Compose Multiplatform
*   **Backend** : Ktor (Server & Client)
*   **Base de données** : SQLite / JetBrains Exposed
*   **Génération PDF** : Apache PDFBox
*   **QR Codes** : ZXing
*   **IA** : LangChain4j + Ollama

---

## Démarrage Rapide

### Prérequis
*   JDK 17 ou supérieur
*   Android Studio / IntelliJ IDEA (dernière version recommandée)
*   Xcode (pour le développement iOS)

### Lancement du Serveur
Le serveur est nécessaire pour toutes les applications électeurs.
```bash
./gradlew :server:run
```

### Lancement de l'App Administration (Desktop)
```bash
./gradlew :adminApp:run
```

### Lancement des Clients Électeurs
*   **Android** : `./gradlew :androidApp:assembleDebug`
*   **Web (Wasm)** : `./gradlew :webApp:wasmJsBrowserDevelopmentRun`
*   **iOS** : Ouvrez le dossier `/iosApp` dans Xcode.

---

## Configuration

Le backend peut être configuré via le fichier [`server/src/main/resources/application.yaml`](./server/src/main/resources/application.yaml).
*   **JWT** : Le secret est généré automatiquement au premier lancement dans `~/.voteparentseleves/jwt-secret.key`.
*   **SMTP** : La configuration pour l'envoi des mails d'identifiants se fait directement dans l'application d'administration.

---

## Tests

Vous pouvez lancer les tests sur les différents modules avec :
*   **Android** : `./gradlew :shared:testAndroidHostTest`
*   **Web** : `./gradlew :shared:wasmJsTest`
*   **iOS** : `./gradlew :shared:iosSimulatorArm64Test`

---
