# 🗺️ Roadmap — VoteParentsEleves
> Projet Kotlin Multiplatform · CNAM M1 · 2025-2026
> À utiliser comme contexte pour Claude Code à chaque session.

---

## Modèle d'architecture retenu

**Déploiement « kiosque par école » — aucune infra centrale.**

Chaque école héberge ses propres données, sur son propre poste admin
(secrétariat / direction). Pas d'hébergement cloud, pas de coût récurrent,
données souveraines, RGPD géré par l'établissement.

```
┌──────────────────────────────────────────────────────────────────┐
│  POSTE ADMIN ÉCOLE (Windows ou Linux)                            │
│  ┌────────────────────┐    ┌────────────────────┐                │
│  │  adminApp          │    │  Ktor embarqué     │                │
│  │  (Compose Desktop) │ ── │  port 8080 LAN     │ ── SQLite .db  │
│  │  UI direction      │    │  (module :server)  │                │
│  └────────────────────┘    └────────────────────┘                │
└────────────────────────────────────┬─────────────────────────────┘
                                     │ Wi-Fi local école
                ┌────────────────────┼────────────────────┐
                ▼                    ▼                    ▼
        [tél parent Android]  [tél parent iOS]    [tél parent …]
            app électeur         app électeur       app électeur
        scan du QR code affiché par adminApp → connait l'URL serveur
```

**Backup** = `VACUUM INTO 'sauvegarde-AAAAMMJJ.db'` produit un fichier
unique consistant, peu importe le mode journal SQLite.

---

## Structure du monorepo

| Module        | Cible                          | Rôle                                                    |
|---------------|--------------------------------|---------------------------------------------------------|
| `:shared`     | KMP (Android/iOS/JVM/Wasm/JS)  | Modèles métier, ApiClient, ViewModels, theme, AlgoHare  |
| `:server`     | JVM                            | Ktor + Exposed + auth + toutes les routes API           |
| `:androidApp` | Android                        | APK électeur                                            |
| `:iosApp`     | iOS                            | App électeur                                            |
| `:adminApp`   | JVM Desktop (Win/Linux/macOS)  | UI direction + embed `:server` au démarrage             |
| `:webApp`     | Wasm / JS                      | Démo navigateur (optionnel)                             |

Stack : Kotlin, Compose Multiplatform, Ktor, Exposed, SQLite (WAL),
HikariCP. **MariaDB définitivement retiré** — SQLite par école est le bon outil.

---

## Phase 1 — Fondations `commonMain` ✅
- [x] Data classes métier
- [x] Interface `AuthenticationProvider` (extensible NFC en V2)
- [x] Interface `AlgorithmeVote`
- [x] Sealed states (StatutScrutin, RoleUtilisateur, AuthResult)

## Phase 2 — Serveur Ktor + Base de données ✅
- [x] Setup Ktor (CORS, Compression, HSTS, StatusPages, Resources, OpenAPI)
- [x] Schéma Exposed avec séparation **anonymat** (BulletinsTable sans FK électeur, EmargementsTable séparée)
- [x] Routes lecture publique : `GET /scrutins[/{id}|/{id}/listes|/{id}/resultats]`
- [x] Route vote : `POST /scrutins/{id}/voter` (atomique bulletin + émargement)
- [x] Routes admin : `POST /scrutins`, `POST /scrutins/{id}/listes`, `PUT /scrutins/{id}/{ouvrir|fermer}`, `POST /admin/electeurs/import`
- [x] Algorithme de Hare (plus forts restes) + 10 tests
- [x] **SQLite hardening** : WAL, busy_timeout=5s, FK ON, synchronous=NORMAL, pool HikariCP (5 connexions)

## Phase 3 — Sécurité & Auth ✅
- [x] PasswordHash PBKDF2 HMAC-SHA-256 (sel par utilisateur, compare constant-time)
- [x] JwtConfig + JwtIssuer (deux realms : électeur scopé scrutin, admin court)
- [x] CodeBasedAuthProvider (impl. Phase 1)
- [x] AuthRepository (verifierElecteur / verifierAdmin / definirMotDePasseElecteur / creerAdmin)
- [x] Routes `POST /auth/login` et `POST /auth/admin/login`
- [x] Protection des routes admin / vote par realm
- [x] Vérification claim scrutinId vs path dans la route vote (anti-usurpation)
- [x] **Secret JWT généré + persisté au 1er run** (`SecretStore.kt`, `~/.voteparentseleves/jwt-secret.key`)
- [x] **Rate limiting** sur `/auth/*` (ktor-server-rate-limit, 5 essais/min/IP, `RateLimiting.kt`)
- [x] **Audit du LogLevel** Ktor — `CallLogging` volontairement non installé (zéro risque de log de body), documenté dans `Monitoring.kt`

## Phase 4 — UI Compose électeur ✅ (charpente)
- [x] Theme pop-art (Couleurs, Typo XL, Formes très arrondies)
- [x] Composants `BoutonClay`, `CarteGlass`, `CartePleine`, `ChampTexte`, `FondKandinsky`
- [x] `EcranLogin` (avec LoginViewModel, vraie auth)
- [x] `EcranListesPresentation` (vue pédagogique des listes, fetch via ListesViewModel)
- [x] `EcranVote` (sélection, vote blanc, lien revoir listes)
- [x] `EcranConfirmation` (avec VoteViewModel, vraie POST /voter)
- [x] `EcranRecu` (vrai bulletinId reçu du serveur)
- [x] Branchement Ktor client end-to-end (3 ViewModels MVVM, ApiClient, expect/actual engines)
- [x] **Découverte serveur école par QR code** (`EcranDecouverte`, scan au
      démarrage via `ScannerQrPlein` expect/actual, parsing `QrPayload` partagé ;
      fallback saisie manuelle + serveur de démo). Sélection automatique du
      scrutin si un seul est ouvert, sinon `EcranChoixScrutin`.
- [x] **Accessibilité TTS** (Android `TextToSpeech` câblé et testé via build ;
      iOS `AVSpeechSynthesizer` écrit en best-effort, non compilable/testable
      sans Mac/Xcode dans cet environnement) :
      bouton « 🔊 Lire à voix haute » sur les 5 écrans du parcours vote.
      Auto-annonce et détection TalkBack/VoiceOver **non implémentées**
      (hors scope de cette passe, déclenchement manuel uniquement).

> **❌ Biométrie retirée du périmètre.** Le facteur d'authentification
> code + mot de passe + QR remis en main propre par l'école est jugé
> suffisant (équivalent au vote par correspondance papier). La biométrie
> exclurait par ailleurs les parents avec téléphones non compatibles.

## Phase 5 — UI Compose admin (`:adminApp`) — EN COURS
> 🎯 App Desktop pour le poste école. Embarque Ktor + SQLite, sert les téléphones via Wi-Fi local.
- [x] Bootstrap module Gradle `:adminApp` (Compose Desktop, JVM)
- [x] `fun main()` démarre `embeddedServer(Netty, 8080)` en background + ouvre la fenêtre Compose
- [x] Écran login admin (réutilise `AuthRepository` via API)
- [x] Dashboard scrutins (liste + badges colorés par statut)
- [x] **Formulaire création de scrutin** (école, dates, sièges)
- [x] **Écran détail d'un scrutin** + actions ouvrir / fermer
- [x] **Gestion des listes candidates** + saisie/import des candidats
- [x] **Import liste électorale par CSV** (`ParseurCsv.kt`, prévisualisation,
      validation — depuis l'écran détail d'un scrutin, section « Identifiants électeurs »)
- [x] **Génération des identifiants + QR codes parents** (PDF à imprimer/découper) :
      code+mot de passe générés et hachés côté serveur (`genererElecteurs`,
      6 chiffres aléatoires), fiches A4 PDFBox+ZXing une par parent
      (`GenerateurPdf.kt`, `GenerateurQr.kt`)
- [x] **Écran « QR code à projeter »** (`EcranProjectionQr`) pour que les
      téléphones découvrent l'URL du serveur école (détection IP LAN auto)
- [x] **Dépouillement** : route `PUT /scrutins/{id}/depouiller`, bouton dans
      `EcranDetailScrutin`, affichage résultats Hare (voix/sièges par liste)
- [x] **Export procès-verbal en PDF** (`genererPdfProcesVerbal`)
- [x] **Backup / Restore** : `VACUUM INTO` pour export, restauration à chaud
      avec recréation du pool HikariCP (`BackupRepository.kt`, `DbHolder`),
      écran dans Paramètres
- [ ] **Packaging natifs** : `:adminApp:packageMsi` (Windows), `:packageDeb` (Linux)
      — non testé (nécessite un run sur la plateforme cible), commandes prêtes

## Phase 6 — Tests & Documentation
- [x] Tests unitaires AlgoHare (10)
- [x] Tests intégration Ktor (Resultats, Vote, Admin, Auth, génération
      identifiants, dépouillement, rate limiting, backup/restore — 29 tests serveur)
- [x] Tests unitaires `QrPayload` (parsing/construction, 4 tests partagés)
- [x] Seed démo idempotent côté serveur
- [ ] **Tests UI automatisés** (smoke login → vote → reçu) — non faits : pas
      d'infra `composeUiTest` configurée dans cette passe. Couverture
      actuelle = compilation de toutes les cibles (jvm/android/wasmJs) +
      tests logique métier ci-dessus. Validation manuelle recommandée avant rendu.
- [ ] **Manuel utilisateur côté école** (PDF court : installer l'admin, créer scrutin, imprimer QR, ouvrir, fermer, exporter PV, sauvegarder)
- [ ] Manuel parent (1 page : scan le QR, entre ton code, vote)
- [ ] Documentation déploiement (installateur Windows + paquet Linux + premier démarrage)
- [ ] Jeu de données démo réaliste

## Phase 7 — Bonus IA (optionnel, dernière étape) 🎁
> 🎯 Demande pédagogique du prof CNAM. **Strictement bonus** : toutes les
> features IA sont désactivables et l'application est 100 % fonctionnelle
> sans aucun LLM. Aucune dépendance dure ajoutée au build de base.
>
> **Architecture :** nouvel onglet « Options IA » dans `:adminApp` qui propose
> deux modes de connexion à un LLM externe au build :
> 1. **Ollama local** (recommandé) — l'écran « Options IA » affiche un bouton
>    « Télécharger Ollama » + lien direct vers `ollama.com/download` + une
>    commande à copier pour pull Mistral 7B. L'app détecte Ollama via un ping
>    `localhost:11434`. Souverain, gratuit, offline, parfait alignement avec
>    le reste du projet (données locales).
> 2. **API distante Mistral** (fallback) — pour les écoles qui ne veulent pas
>    installer Ollama, formulaire pour saisir une clé API Mistral La Plateforme.
>    Disclaimer fort affiché : *« les textes envoyés sortent du réseau école »*.
>
> Tant qu'aucun des deux n'est configuré, les boutons IA dans les autres écrans
> sont masqués / désactivés. L'expérience admin de base reste inchangée.
>
> **Features IA livrées :**
> - [ ] **Import intelligent d'électeurs** : grosse zone de texte « collez vos
>       électeurs ici (n'importe quel format) » → LLM structure en JSON
>       `{ id, nom, prenom, ecoleId }` → prévisualisation table → validation
>       humaine → POST `/admin/electeurs/import`. Détection bonus de doublons
>       probables (« Dupont A. » vs « Dupont Alice »).
> - [ ] **Génération du kit communication parents** : après création d'un
>       scrutin + import électeurs, bouton « Préparer le kit » génère :
>       - Un PDF d'une page A4 par parent (lettre personnalisée + QR code +
>         code en clair) prêt à imprimer/découper
>       - Brouillon du mail de convocation (à copier-coller dans le client mail)
>       - Optionnel : version simplifiée FR A2 pour familles allophones
>
> **Pourquoi en dernier :** ça demande à l'utilisateur (l'école) une action
> d'installation ou de config externe. Donc on s'assure d'abord que tout le
> reste (Phases 1-6) fonctionne en autonomie complète. L'IA est la cerise.

---

## Backlog V2 (post-rendu / commercialisation)
- [ ] Module NFC/JavaCard (`JavaCardNFCAuthProvider` — branché sur l'interface Phase 1)
- [ ] Chiffrement homomorphe des bulletins
- [ ] Certification ANSSI (CSPN)
- [ ] HTTPS local (mkcert ou cert auto-signé + trust côté app électeur)
- [ ] Discovery serveur via mDNS / Bonjour (en plus du QR code)
- [ ] Mode multi-établissements (rectorat agrégeant plusieurs `.db` école)
- [ ] **Assistant LLM embarqué côté électeur** (Gemini Nano / MediaPipe LLM)
      pour répondre aux questions des parents. Hors V1 par souci d'accessibilité
      universelle (devices entry-level non supportés) et de risque d'hallucination
      sur sujet civique sensible.

---

## Audit sécurité — état actuel

| Risque | Couvert ? | Détail |
|---|---|---|
| SQL injection | ✅ | Exposed = prepared statements partout, zéro SQL raw |
| XSS | ✅ N/A | API JSON, pas de render HTML user-controlled |
| CSRF | ✅ N/A | JWT en Authorization Bearer, pas de cookies de session |
| Anonymat bulletin | ✅ | Schéma BDD garantit séparation identité/vote |
| Unicité du vote | ✅ | PK (electeurId, scrutinId), transaction atomique |
| Hash mot de passe | ✅ | PBKDF2 100k iter, sel par user, compare constant-time |
| Token usurpation | ✅ | JWT HMAC-256, claim scrutinId vérifié vs path |
| Bruteforce login | ✅ | Rate limit 5/min/IP sur `/auth/*` |
| Secret JWT en dur | ✅ | Généré aléatoirement + persisté au 1er run (`SecretStore.kt`) |
| HTTPS local | ⚠️ | Trafic LAN en clair. Wi-Fi école contrôlé = acceptable V1, HTTPS auto-signé en V2 |
| Logs sensibles | ⚠️ | Auditer LogLevel pour ne pas logger les bodies des `/auth/*` |

---

## Conventions techniques (rappel CLAUDE.md)
- Style fonctionnel Kotlin strict : `val`, data class, sealed states, pas d'exceptions métier
- MVVM stricte : ViewModel + state sealed, pas de logique UI dans les composables
- `Result<T>` ou sealed `ApiResult` pour la gestion d'erreurs
- Tests à chaque ajout de logique métier (algo, repo, route)
- Séparation identité/vote en BDD vérifiée par tests d'intégration

---

## Commandes utiles

```bash
# Tests
./gradlew :server:test :shared:jvmTest

# App admin Desktop (lance la fenêtre + Ktor embarqué)
./gradlew :adminApp:run

# Serveur Ktor en standalone (pour dev de l'app électeur seul)
./gradlew :server:run

# App électeur Android sur device USB
adb reverse tcp:8080 tcp:8080
./gradlew :androidApp:installDebug

# Packaging adminApp
./gradlew :adminApp:packageMsi          # installeur Windows
./gradlew :adminApp:packageDeb          # paquet Debian/Ubuntu
./gradlew :adminApp:packageDmg          # macOS

# Démo web
./gradlew :webApp:wasmJsBrowserDevelopmentRun --continuous

# Credentials de la démo (seedés automatiquement)
#   Admin :    admin / admin123
#   Électeurs: parent1 … parent5 / 0000
#   Scrutin :  scr-demo-2026 (statut OUVERT)
```

---

## État au 23 juin 2026 — méga-passe « tout le reste de la roadmap »

Tout le périmètre des Phases 3, 4, 5 et 6 (hors Phase 7 IA, explicitement
laissée de côté) a été implémenté dans une seule session :

- **Phase 3** : secret JWT généré/persisté, rate limiting 5/min/IP, audit log.
- **Phase 4** : découverte serveur par QR (scan Android réel via
  zxing-android-embedded, iOS en best-effort non testé), sélection de
  scrutin si plusieurs ouverts, bouton TTS sur les 5 écrans du parcours vote.
- **Phase 5** : import CSV électeurs, génération identifiants+mots de passe
  (PBKDF2, retournés en clair une seule fois), fiches PDF+QR par parent,
  écran QR de projection (détection IP LAN), dépouillement, export PV PDF,
  backup/restore SQLite à chaud.
- **Phase 6** : 29 tests serveur + 4 tests partagés ajoutés ; pas de tests
  UI automatisés (non configurés, validation manuelle recommandée) ; pas de
  manuels PDF rédigés (hors code).

Limitations connues à vérifier en priorité avant un rendu :
- L'implémentation iOS du scanner QR et du TTS (`ScannerQr.ios.kt`,
  `GuideVocal.ios.kt`) a été écrite sans Mac/Xcode disponible — à compiler
  et tester dès qu'un environnement Apple est accessible.
- Packaging natifs (`packageMsi`/`packageDeb`/`packageDmg`) non exécutés
  dans cette session (nécessitent la plateforme cible).
- Tous les autres modules (`:server`, `:shared` jvm/android/wasmJs,
  `:adminApp`, `:androidApp`) compilent et leurs tests passent.

---

*Dernière mise à jour : 23 juin 2026*
