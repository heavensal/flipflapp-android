# FlipFlapp Android

Client natif Kotlin / Jetpack Compose pour le MVP FlipFlapp. L’app consomme l’API JSON Rails `/api/v1` ; les règles métier restent côté serveur.

## Documentation

| Sujet | Document |
|---|---|
| Règles agent | [AGENTS.md](AGENTS.md) |
| Scope produit | [docs/PROJECT.md](docs/PROJECT.md) |
| Domaine mobile | [docs/DOMAIN.md](docs/DOMAIN.md) |
| Intégration API | [docs/API.md](docs/API.md) |
| Architecture | [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) |
| Style Kotlin | [docs/KOTLIN_STYLEGUIDE.md](docs/KOTLIN_STYLEGUIDE.md) |
| Design Material | [docs/DESIGN.md](docs/DESIGN.md) (dark only, olive+gold) |
| Sécurité | [docs/SECURITY.md](docs/SECURITY.md) |
| Tests | [docs/TESTING.md](docs/TESTING.md) |
| Dev local | [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md) |
| Release / Play Store | [docs/RELEASE.md](docs/RELEASE.md) |
| Playbook agent | [docs/CODEX_PLAYBOOK.md](docs/CODEX_PLAYBOOK.md) |

Backend (checkout voisin) :

- `../flipflapp-rails/docs/DOMAIN.md`
- `../flipflapp-rails/docs/API.md`
- `../flipflapp-rails/swagger/v1/swagger.yaml`

## Layout source

| Dossier | Rôle |
|---|---|
| `app/` | Composition root, session, navigation |
| `core/api/` | Transport OkHttp + surface `/api/v1` complète |
| `core/models/` | DTOs alignés OpenAPI |
| `core/security/` | Stockage JWT chiffré |
| `core/designsystem/` | Dark olive+gold theme, tokens, shared Compose components |
| `features/` | Auth, events, détails, éditeur, amis, notifs, profil |

## Prérequis

- Android Studio (stable)
- JDK 17+
- SDK Android (`compileSdk` / `targetSdk` 36)
- Rails API locale pour le debug (`bin/dev` dans `flipflapp-rails`)

URL API debug par défaut : `http://10.0.2.2:3000` (localhost de la machine hôte depuis l’émulateur).

Override Gradle :

```bash
./gradlew :app:assembleDebug -Pflipflapp.apiBaseUrl=http://192.168.1.20:3000
```

## Commandes rapides (débutant Kotlin / Android)

À lancer depuis `flipflapp-android/` :

```bash
# Lister les modules Gradle
./gradlew projects

# Compiler l’APK debug
./gradlew :app:assembleDebug

# Installer sur l’émulateur / appareil connecté
./gradlew :app:installDebug

# Lancer les tests unitaires JVM
./gradlew :app:testDebugUnitTest

# Voir les dépendances compile debug
./gradlew :app:dependencies --configuration debugCompileClasspath

# Nettoyer le build
./gradlew clean
```

Dans Android Studio :

1. **Open** → dossier `flipflapp-android`
2. Attendre la sync Gradle
3. Choisir un émulateur / device
4. Run ▶ (ou `Shift+F10`)

Raccourcis utiles Android Studio :

| Action | Raccourci (macOS) |
|---|---|
| Sync Gradle | ⌘ + Shift + I (ou icône éléphant) |
| Run | ⌃ + R |
| Build project | ⌘ + F9 |
| Search everywhere | Shift Shift |
| Go to class | ⌘ + O |
| Go to file | ⌘ + Shift + O |
| Reformat | ⌥ + ⌘ + L |
| Logcat | View → Tool Windows → Logcat |

Notions Kotlin à connaître pour ce repo :

- `data class` → modèles immuables
- `sealed interface` → états UI explicites (`LoadState`)
- `suspend` + coroutines → appels réseau
- `StateFlow` / `collectAsStateWithLifecycle` → état observé par Compose
- `@Composable` → UI déclarative
- `ViewModel` → logique d’écran hors UI

## Surface API couverte

Auth (register, sign in/out, password, confirmation), `me` / users, events CRUD, event teams, event participants, invitations, friendships (buckets + search), notifications (list / read / read_all / delete).
