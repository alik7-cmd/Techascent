This is a Kotlin Multiplatform project targeting Android, iOS, Web.

* `/aiFinder` is the Android/iOS inventory finder. Android system Back follows the
  screen's toolbar back action; Welcome and Home retain normal system exit behavior.
  Its isolated Koin context survives activity recreation, while screen ViewModels
  preserve in-memory state across rotation.
* `/aiFinder` currently uses offline sample scan results: no photo uploads or Vertex API calls
  are made. Review cards support renaming and removing items with Material dialogs on Android
  and native alerts on iOS.
  Edits are retained in memory across rotation. Tap "Save into" on Scan or Review to select a
  location, search by its path, and confirm with "Save location"; dismissing cancels selection.
  Create one with a name, Area/Room/Container type and optional parent. Paths are
  derived from parent IDs. Saving adds the edited items to the selected location and makes them
  searchable. Locations and inventory are in-memory only and reset on process restart.
  To restore live analysis, replace the `MockPhotoAnalyzer` binding in `AiFinderModule`
  with `VertexGeminiPhotoAnalyzer` and its HTTP client/config dependencies, and update the
  demo notices in Scan and Review.
* The retained live photo analyzer uses Gemini on Vertex AI (express mode) through one shared
  Ktor client for Android and iOS. Add the key to the git-ignored `local.properties`:
  `VERTEX_API_KEY=...` (optional: `VERTEX_MODEL=gemini-2.5-flash`), then rebuild.
  The key ships inside the app, so use it for development only; production should call
  Vertex AI through a backend.

* `/composeApp` is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - `commonMain` is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    `iosMain` would be the right folder for such calls.

* `/iosApp` contains iOS applications. Even if you’re sharing your UI with Compose Multiplatform, 
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.


Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html),
[Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/#compose-multiplatform),
[Kotlin/Wasm](https://kotl.in/wasm/)…

We would appreciate your feedback on Compose/Web and Kotlin/Wasm in the public Slack channel [#compose-web](https://slack-chats.kotlinlang.org/c/compose-web).
If you face any issues, please report them on [YouTrack](https://youtrack.jetbrains.com/newIssue?project=CMP).

You can open the web application by running the `:composeApp:wasmJsBrowserDevelopmentRun` Gradle task.