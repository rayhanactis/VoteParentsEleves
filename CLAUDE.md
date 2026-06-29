# VoteParentsEleves — Cahier des charges

## Contexte
Application de vote électronique pour élections de parents d'élèves.
Projet CNAM M1, module développement mobile (4e semestre).

## Stack technique
- Langage : Kotlin uniquement
- UI : Compose Multiplatform (code UI partagé)
- Cibles : Android (minSdk 29), iOS
- Architecture : MVVM strict (découplage ViewModel / UI)
- Backend : Ktor (REST API)
- ORM : JetBrains Exposed
- BDD : SQLite (dev/tests) → MariaDB (prod)

## Style de code
- Style **fonctionnel Kotlin** strict :
  - `val` partout, jamais de `var`
  - `data class` immutables uniquement
  - `sealed class` pour tous les états (StatutScrutin, AuthResult, etc.)
  - Pas d'effets de bord dans la logique métier (ViewModels et services purs)
  - `fold`, `map`, `filter`, `reduce` plutôt que boucles impératives
  - `Result<T>` ou `Either<Error, T>` pour la gestion d'erreurs, jamais d'exceptions
  - Fonctions d'extension plutôt que classes utilitaires

## Fonctionnalités clés
1. Back-office : paramétrage école, listes candidates, plages horaires
2. Vote : affichage listes, vote blanc, confirmation biométrique
3. Biométrie : Android BiometricAPI / iOS LocalAuthentication
4. QR Code : import automatique des identifiants électeurs
5. Algorithme de Hare (représentation proportionnelle au plus fort reste)
6. Résultats : procès-verbal exportable

## Règles métier
- Anonymat obligatoire (séparer identité et vote en BDD)
- Unicité du vote (un parent = un vote)
- Scrutin de liste bloquée (pas de panachage)
- Scellement de l'urne après validation

## Structure cible
- commonMain : modèles, ViewModels, logique métier, algo de Hare, API Ktor client
- androidMain : BiometricAPI, UI Android-specific
- iosMain : LocalAuthentication, UI iOS-specific
- server : Ktor server + Exposed + SQLite/MariaDB