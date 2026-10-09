# AndroidSyncAdapter

Sample of creating your own Android sync adapter, written in Kotlin. It registers a custom account
type, a content provider holding a local list of TV shows, and a sync adapter that two-way syncs
that list with a remote server.

## What it demonstrates

- A custom account type: `AbstractAccountAuthenticator`, a bound service, and sign-in / sign-up screens
- A `ContentProvider` backed by SQLite (`TvShowsContentProvider`)
- An `AbstractThreadedSyncAdapter` and its service (`TvShowsSyncAdapter`, `TvShowsSyncService`)
- Toggling "is syncable" / "auto sync" and requesting a manual sync from the UI

## Project structure

```
app/src/main/java/com/licon/syncadaptertest/
├── MainActivity.kt                  Demo UI: local list, connect, sync controls
├── LogTag.kt                        Log tag shared by the app
├── authentication/
│   ├── AccountGeneral.kt            Account type, token types, backend in use
│   ├── Authenticator.kt             The AbstractAccountAuthenticator implementation
│   ├── AuthenticatorService.kt      Exposes the authenticator to the system
│   ├── AuthRequest.kt               Runs a sign-in / sign-up request off the main thread
│   ├── SignInAuthenticatorActivity.kt
│   ├── SignUpActivity.kt
│   ├── ServerAuthenticate.kt        Interface: userSignIn / userSignUp
│   ├── DemoServerAuthenticate.kt    Offline backend used by default
│   └── ParseComServer.kt            Small HTTP helper for the Parse.com REST API
├── db/                              Content provider, contract, SQLite helper and the TvShow model
└── syncadapter/                     Sync adapter, its service and the remote accessor
```

## Build

Requirements: JDK 17 and the Android SDK (platform 34).

1. Create `local.properties` in the project root, pointing at your SDK
   (Android Studio does this for you):
   ```
   sdk.dir=/path/to/Android/sdk
   ```
2. Build and install:
   ```
   ./gradlew assembleDebug
   ./gradlew installDebug
   ```
   The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

In Android Studio, open the root folder (not `app`) and let it sync.

Tech: Android Gradle Plugin 8.7, Gradle 8.9, Kotlin 2.0, `compileSdk`/`targetSdk` 34, `minSdk` 21,
Gson. The UI uses the platform Material theme, so there are no AndroidX dependencies.

## Using the app

1. Tap **Connect** and sign up or sign in. With the default backend any valid-looking email and a
   password of 6+ characters works.
2. Add TV shows to the local list, then enable **IsSyncable** / **Auto sync** or tap **Sync now**.

## Using your own backend

The authenticator only talks to a backend through `ServerAuthenticate`:

```kotlin
fun userSignIn(user: String, pass: String, authType: String): String?
fun userSignUp(name: String, email: String, pass: String, authType: String): String?
```

Each method returns an auth token. To use your own server, implement this interface and set
`serverAuthenticate` in `AccountGeneral` to your implementation. Also change `ACCOUNT_TYPE`
in `AccountGeneral` and `accountType` in `app/src/main/res/xml/authenticator.xml` to your own value.

## Limitations

- Parse.com has been shut down. Sign-in and sign-up therefore use `DemoServerAuthenticate`, an offline
  stand-in that stores no users and returns a random token.
- The TV show sync (**Show remote List**, **Sync now**) still calls the Parse.com REST API in
  `ParseComServerAccessor`, so it fails until you point it at a working backend such as Parse Server.
- The `AUTHENTICATE_ACCOUNTS`, `MANAGE_ACCOUNTS` and `USE_CREDENTIALS` permissions are deprecated
  or ignored on recent Android versions, so behaviour on new devices may differ.
- It is a learning sample, not production code.

## License

[MIT](LICENSE) © 2016 Khairul Alam Licon
