# Where Did I Put It? — aiFinder

Figma reference: https://www.figma.com/design/1jdKJp7TJxJLVcSBRZalzr

## Product promise
Photograph a drawer, shelf or storage box. Confirm the objects recognized by AI. Later ask where an object is and get the **last known location and source photograph**. The user, not AI, is the authority on item placement.

## MVP scope
1. Capture a location photo and link it to a room / cabinet / drawer / box.
2. AI proposes visible objects and categories; users correct or confirm them.
3. Search inventory with natural language and synonyms, favoring fast local metadata search.
4. Show last confirmed location and source photo / highlighted object where supported.
5. Move, edit, remove and manually add items.
6. Store an offline inventory and basic usage accounting.

**Current implementation:** camera capture, editable scan review, Android and iOS Google/Firebase authentication, Gemini (Vertex AI) photo analysis, and user-scoped Firebase locations and items with a persistent Room cache. Home, spaces, search and item details read from that cache. iOS Firebase location/item access still needs native integration.

## Location cache
Room Multiplatform stores locations in `aifinder.db` on Android and iOS, using a shared entity/DAO and bundled SQLite driver. Rows are keyed by Firebase user ID and location ID. The repository reads Firebase only when that user's local cache is empty; subsequent reads observe Room. Creating a location writes Firebase first and then upserts Room. Remote failures are surfaced without inserting a local-only location.

Room adopts the existing version-1 SQLite cache without deleting its records. KSP generates the platform database constructors; exported Room schemas live in `aiFinder/schemas`. Commit schema changes alongside future migrations.

This is offline-first, not live cross-device synchronization:
- Locations and items load from Room, with no network call, whenever the cache has data.
- When the cache is empty, Firebase is read with a 10-second timeout. If that read fails or times out (for example offline), the app shows the empty cache instead of an error and tries Firebase again the next time it observes.
- Changes from other devices are not fetched while the cache has data.

Home shows a shimmer skeleton of the spaces list until the cache emits.

Space details show a mint location summary, saved-item cards with nested location paths, and a persistent scan action. Item details show the original scan photo, category, a step-by-step last-known location path, and notes when present. Both screens use the shared empty/error cards. Move and edit actions still display the existing unavailable notice; this visual refresh does not implement inventory mutations.

## Inventory cache
Scanning requires a selected storage location. Analyze stays disabled with a warning until a location is selected; the selection carries over to review, where it can be changed before saving.

### Development: fake analysis
Set `AnalysisConfig.USE_FAKE_ANALYSIS` in `aiFinder/src/commonMain/kotlin/org/techascent/aifinder/config/AnalysisConfig.kt`, then rebuild and restart the app. It is currently `true`: Android and iOS return sample HDMI cable, passport and key detections without creating the AI HTTP client or calling Vertex AI, so analysis incurs no Google Cloud AI charges and needs no Vertex API key. Samples ignore the photo and omit bounding boxes rather than inventing positions. Set it to `false` before shipping to restore real analysis.

Only recognition is faked. Authentication, location selection, scan-credit checks/deductions, and saving photos/items still use their existing implementations and may make Firebase calls. Successful fake scans still cost one app scan credit. There are no account-based credit bypasses. Saving sample results persists sample inventory and the captured photo normally.

The Home/Scan credit card prepares purchases inline: Get more scans becomes a spinner while access and store identity are checked. Preparation failures stay on the card; leaving the screen cancels preparation. Only the ready paywall opens a modal. Capturing a photo is local; `MeteredPhotoAnalyzer` must successfully refresh remote access before invoking either analyzer. Zero credits or an access-check failure never calls Vertex AI. Purchase completion resumes the pending photo through that same access gate, not directly through the AI implementation.

Scans are analyzed by `VertexGeminiPhotoAnalyzer` using `VERTEX_API_KEY` and `VERTEX_MODEL` from the git-ignored root `local.properties`. Reviewed items are saved to `aifinder/users/{uid}/items/{itemId}` in one atomic Realtime Database update (name, category, tags, locationId, photoId, confidence, createdAt), then upserted into the Room `CachedItem` table. Like locations, items are fetched from Firebase only when the user's item cache is empty. A failed save keeps the review results and shows the error.

`ObserveInventoryUseCase` combines cached locations and items into an `InventorySnapshot`, which provides:
- Home's top-level spaces, with descendant space and item counts.
- See all, listing every location ordered by path.
- Space details, showing items in a location and its nested locations.
- Search over item name, category, tags and location path. Question words such as "where is my" are ignored.

Room migration 3-to-4 adds `CachedItem`. Sign-out resets the inventory ViewModels; cached rows stay keyed by user ID.

## Photo storage

Each scan photo is uploaded to Firebase Storage at `aifinder/users/{uid}/photos/{photoId}.jpg`. The bucket is set by `FIREBASE_STORAGE_BUCKET` in `local.properties`, e.g. `gs://pmscs-a2eed.firebasestorage.app`. Items are saved only after the upload succeeds.

The photo is also cached on the device for each user: in Android app files and in iOS Application Support. Search, space and item screens read the cache first. They download from Storage only when the local copy is missing, and the result is kept in a small in-memory cache. iOS can show cached photos but can't upload until the Firebase iOS SDK is added.

You need Storage rules that allow `aifinder/users/{uid}/**` when `request.auth.uid == uid`.

## Startup routing
### App icon
Android and iOS share a deep-teal storage-drawer and magnifying-glass mark, with AI sparkles inside the lens and mint scan brackets. Android includes adaptive and Android 13 themed icons, plus density-specific legacy icons. iOS includes opaque iPhone, iPad and App Store assets. Regenerate the artwork with `python3 aiFinder/tools/generate_app_icon.py` (requires Pillow); the generator owns the shared geometry, colors, raster exports and Android foreground vectors.

Welcome and the first Login slide share `WelcomeIllustration`, a Compose Canvas drawing of the drawer, stored objects, storage box and magnifying glass. It has no embedded background or bitmap dependency; its surface, accent, outline and inset colors can be supplied by a future theme. The surrounding mint panel remains owned by each screen.

Welcome checks the restored Firebase session and a device-local Room welcome flag while showing only the background (no content or spinner). Signed-in users go directly to Home. Signed-out users see welcome content once per app data installation; subsequent launches go directly to Login. The flag is saved before the first welcome content is shown. Continuing replaces Welcome with Login, and Login has no route back to Welcome. Room migration 2-to-3 adds the flag table without changing cached locations.

## Navigation
### Development credit-card comparison
Home uses the compact `ScanBalanceCardV3`, with the Deep Teal gradient, real balance and inline Get more/retry actions. Scan retains the larger `ScanBalanceCard` design. `CreditCardPreviewConfig.SHOW_COMPARISON` controls an optional comparison below Home's real card; it is currently disabled. Enable it to compare Deep Teal, Soft Mint and Compact Wallet with sample available, zero and loading balances. Preview purchase buttons do nothing.

All back-enabled screens use the shared `ScreenTopBar`: a fixed back chevron and left-aligned title with safe-area padding and an accessible heading. Account, Search, Scan, Review, Spaces, Space and Item Details share the same sizing and spacing; titles are not duplicated in the scrolling content.

`AiFinderNavigation` uses Navigation Compose's multiplatform `NavHost`, with all routes defined in the sealed `AiFinderScreen` class. Space and item details receive encoded IDs as navigation arguments. Back returns to the actual originating screen, including when opening Scan from a space. Welcome completion, sign-in and sign-out replace the back stack, preventing Back from reopening authentication screens or a signed-out session. Saving a review returns to Home and removes the scan/review flow from the stack. Shared ViewModels retain the scan-to-review handoff and existing logout cleanup.

## Account settings
After authentication, Android and iOS update `aifinder/users/{uid}/signedIn` with `platform` (`ANDROID` or `IOS`), `provider` (`Google` or `Apple`), and `deviceName`. This stores the latest sign-in, not a history, and leaves locations/items untouched. The write is best-effort with a 15-second timeout: failures are logged without blocking successful login. Realtime Database rules must permit writes to this node only when `auth.uid == uid`. Restored sessions do not trigger another write.

Device names require no permissions: Android uses manufacturer/model and iOS uses `UIDevice.name` (often a generic name such as “iPhone”).

The account icon on Home opens `AccountScreen`. It shows the signed-in Firebase user's name, email, initial and sign-in provider (Google or Apple). The Subscription, Household & sharing and Privacy & data cells are placeholders for now. Log out asks for confirmation in a native dialog (Material `AlertDialog` on Android, `UIAlertController` on iOS). It then calls `SignOutUseCase`, clears the location state and returns to Login.

## Monetization proposal (hypotheses to validate)
Account's Subscription row opens a compact, content-sized modal bottom sheet over Account, matching Choose Location's padding, spacing, rounded top corners, and drag handle. It shows credit status, selectable 3-scan and 5-scan preview packs, and a short purchase-availability notice, without a hero illustration or promotional copy. Pack cards use a soft mint selected background, left-aligned titles and prices, a right-side radio indicator, and a subtle best-value label. Selection survives screen recreation while open; closing, swiping down, or pressing back dismisses the sheet. Purchases use RevenueCat (see below). When RevenueCat is configured and the user is out of credit, the sheet shows the credit status above RevenueCat's server-driven `Paywall` UI. Otherwise it shows the preview packs (illustrative USD prices) with a disabled "Purchases unavailable" button.

## RevenueCat purchases
- **UI:** `SubscriptionSheet` shows RevenueCat's dashboard-designed `Paywall` full screen only after purchase preparation and user identification succeed. Loading, crediting, errors, missing configuration and delayed credits use an app-styled status dialog with retry/cancel where appropriate.
- **SDK:** `purchases-kmp-core` and `purchases-kmp-ui` `4.0.0`, using Kotlin 2.3.20 and Compose Multiplatform 1.9.3.
- **Keys:** add `REVENUECAT_ANDROID_API_KEY` and `REVENUECAT_IOS_API_KEY` to the git-ignored root `local.properties`. A RevenueCat **Test Store** (sandbox) public key works for development without Play/App Store setup. A blank key disables purchases on that platform; the app keeps working and shows the preview sheet. The keys are generated into `config/RevenueCatConfig`. Use public SDK keys only; never use secret keys.
- **Dashboard setup:** create consumable products `scan_pack_3` and `scan_pack_5`, add them as packages to the **current offering**, and attach a paywall to it. No entitlement is needed, because credits live in Firebase.
- **Identity:** `RevenueCatPurchaseGateway` configures the SDK lazily, with the Firebase UID as `appUserId`. Login calls `logIn(uid)` (best-effort), and logout calls `logOut()`. The paywall is shown only after the user is identified, so a purchase is never attached to an anonymous RevenueCat ID.
- **After purchase:** the app records the transaction and credits the wallet, falling back to polling (8 × 2 s) if recording fails. The sheet shows "Adding your scans…" while crediting. A pending analysis automatically resumes using the same captured photo once access is confirmed; no second capture or Analyze tap is needed. Late credits show a retryable pending state. Dismissing purchases or leaving Scan discards the pending photo.
- **Granting credits (client):** when the paywall reports `onPurchaseCompleted`, the app writes the `StoreTransaction` to `aifinder/users/{uid}/purchases/{transactionId}` (`purchaseId`, `productId`, `scansGranted`, `purchasedAt`, `store`, `status` `pending` → `credited`) and, in one Firebase transaction, adds 3 (`scan_pack_3`) or 5 (`scan_pack_5`) to `scanWallet/remainingScans`, sets `activeSubscription: true`, and marks `scanWallet/creditedPurchases/{transactionId}` so a purchase is never credited twice. Invalid key characters in transaction IDs are replaced with `_`. If the write fails, the app polls the wallet in case a backend credits it. Because the client can write its own wallet, a RevenueCat webhook plus tighter database rules is still recommended before production.
- **iOS:** RevenueCat 4 includes its native implementation; do not add `PurchasesHybridCommon` or `PurchasesHybridCommonUI` through SPM/CocoaPods. The iOS deployment target is 15+. Note that the iOS Firebase credit adapter is still unavailable.

### Running the separate iOS app

Open `aiFinderIos/aiFinderIos.xcodeproj` from the repository root and select the `aiFinderIos` scheme. The build phase runs `:aiFinder:embedAndSignAppleFrameworkForXcode` and the SwiftUI host launches `AiFinderApp` through `MainViewController`. Java 17 is required; the script discovers it with `/usr/libexec/java_home` when `JAVA_HOME` is unset. For a physical device, select your signing team in Xcode.

The existing `iosApp` project still runs `composeApp` and is unchanged. Xcode merges generated app metadata with `aiFinderIos/aiFinderIos/Info.plist`, which sets the Boolean `CADisableMinimumFrameDurationOnPhone = YES` required by Compose at startup. Preserve this Boolean if you replace the plist. Add your own permission descriptions (including camera usage) to this plist before using the camera. Firebase inventory, photo and wallet adapters on iOS are still placeholders.

### iOS Google sign-in

The Swift host links FirebaseCore, FirebaseAuth, FirebaseDatabase and GoogleSignIn through Swift Package Manager. It configures Firebase and installs `FirebaseAuthBridge` before creating the Compose view, then forwards `.onOpenURL` to Google Sign-In. The iOS Kotlin `IosGoogleAuthDataSource` and `IosSignInDataSource` adapt the bridge callbacks to suspending operations, preserving cancellation and shared auth error handling. Firebase's persisted current user drives startup routing; logout clears the existing local caches and signs out of Firebase and Google. No OAuth tokens are stored by the bridge.

Register bundle ID `org.techascent.aifinder` in the same Firebase project as Android and enable the Google Authentication provider. Download `GoogleService-Info.plist`, add it to the aiFinderIos target's Copy Bundle Resources exactly once, and keep it local (git-ignored). The bridge reads `CLIENT_ID` and `DATABASE_URL` from Firebase's configured options. In the app's `Info.plist`, set `CFBundleURLTypes` / `CFBundleURLSchemes` to that file's `REVERSED_CLIENT_ID`; update it when switching Firebase projects. Never embed an OAuth client secret. The selected Firebase 13 and Google Sign-In 10 packages support the project's iOS 15 minimum.

Verify Google login and cancellation on a simulator/device, check the Firebase Authentication user and `aifinder/users/{uid}/signedIn` record, then relaunch to verify session restoration and log out to verify session/cache cleanup. Apple sign-in remains unavailable.

## Prepaid scan access: model and flow

Scan access now uses **prepaid credits**, not a recurring subscription: `remainingScans > 0` means subscribed, and zero means inactive. Every account requires credits. The sheet displays the real balance when it can be loaded; packs are bought through RevenueCat.

Realtime Database structure (example values, not seeded by the app):

```json
{
  "aifinder": {
    "users": {
      "FIREBASE_USER_UID": {
        "scanWallet": {
          "remainingScans": 4,
          "activeSubscription": true,
          "usedScans": {
            "PHOTO_ID_ALREADY_CHARGED": true
          }
        },
        "purchases": {
          "VERIFIED_STORE_TRANSACTION_ID": {
            "purchaseId": "VERIFIED_STORE_TRANSACTION_ID",
            "productId": "scan_pack_5",
            "scansGranted": 5,
            "purchasedAt": 1793700000000,
            "store": "google_play",
            "status": "verified"
          }
        }
      }
    }
  }
}
```

- **Sign-in:** authenticate, save sign-in metadata (best-effort, 15 s timeout), load credit access, and identify the user with RevenueCat, then emit SignedIn. None of these follow-up steps can block login: failures are logged and access is re-checked when scanning. A transaction creates a missing wallet with `remainingScans: 0` and `activeSubscription: false`. Existing balances and usage history are preserved; the active flag is reconciled to `remainingScans > 0`. Missing balance fields are initialized to zero, while malformed/negative balances produce an error rather than being reset. Restored sessions check again when scanning.
- **Scan entry:** Home/Spaces scan actions always open Scan without an access gate. Users can preview the camera and select a location regardless of credits. Analyze captures the photo and checks authoritative access through the metered analyzer before calling AI. Insufficient credits open the purchase sheet and retain that photo in memory for automatic resumption. Connection/permission failures show an analysis error rather than treating the user as out of credits. Inventory browsing remains offline-first; checking and spending cloud credits requires Firebase access.
- **Scan balance display:** Home and Scan share a mint credit card showing remaining credits from a per-user Room wallet cache, calling Firebase only on a cache miss. Remote refreshes, successful credit deductions, and purchases update this cache. A cached zero is a valid balance, not a cache miss. Scanning still checks the remote wallet; cached credits never authorize spending. The Room v4-to-v5 migration preserves existing inventory and preferences. At zero, Analyze remains available once a location is selected; a "Get more scans" action also allows purchasing directly. A visible notice explains that each successful photo scan costs one credit even when the user does not save its findings.
- **Logout cleanup:** clears the signing-out user's cached items, locations, scan wallet and photo files, plus the in-memory photo cache and screen state. Cloud records are not deleted. The welcome preference remains on the device. Cleanup failures surface through the logout error UI, allowing retry.
- **Successful scan:** the metered analyzer checks access again before calling Gemini. After analysis produces usable detections, a Realtime Database transaction decrements the wallet and records the photo ID in `usedScans` together. Duplicate consumption of the same ID does not deduct twice; concurrent transactions cannot make the balance negative. Review is shown only after consumption is acknowledged. Empty recognition, camera failures, and AI errors do not spend a credit. Saving, renaming, or deleting reviewed items does not spend another credit or refund it.
- **Exhaustion:** spending the last credit atomically sets `activeSubscription: false` alongside the balance deduction. The balance remains authoritative for access; every wallet refresh repairs a stale active flag. Earlier purchases and scan IDs remain intact. Sign-out cancels pending gate checks so the next user cannot inherit access.
- **Purchases:** the app records each verified store transaction once (keyed by transaction ID) and credits the wallet atomically; it never replaces the wallet or erases prior purchase records. Refunds/revocations must still be handled server-side.

**Required rules before use:** permit users to read only their own wallet and purchase history. Only the Admin SDK/console may grant credits or create purchase records. Allow initial wallet creation only at zero credits with `activeSubscription: false`. Wallet client transactions must be constrained to decreasing the current balance by exactly one (or keeping the balance unchanged during initialization/reconciliation/idempotent retry), with `activeSubscription == (remainingScans > 0)`; deny increases, deletes, invalid balances, and changes to existing usage IDs. Credit-granting backends must update the active flag atomically too. Do not enable broad writes at `aifinder/users/{uid}`: parent grants would bypass narrower restrictions. Integrate these restrictions with the existing location/item/sign-in rules.

**Production boundary:** this is app-side gating/accounting, not a tamper-proof billing backend. Gemini is still called directly by the app; a modified client could skip the gate, and simultaneous analyses may run before one loses the final-credit race. A timed-out/cancelled database transaction can still commit; the same photo ID is safe to reconcile/retry, but a newly captured photo has a new ID. Before charging real users, move analysis and consumption into an authenticated server operation with a persistent request ID, credit reservation, result recovery, and failure refunds. Keep the Firebase usage ledger for idempotency/reconciliation. iOS has the shared flow/model but its Firebase credit adapter explicitly reports unavailable until native integration is added.

| Plan | Price hypothesis | Proposed limits |
|---|---|---|
| Free | $0 | 3 storage locations, 50 items, 10 total AI scans, local search |
| Plus | $2.99/month or $24.99/year | Unlimited manual entries and locations, 100 AI scans/month, sharing and optional cloud backup |
| Lifetime | $49.99 once (test later) | Local inventory permanently; limited included AI/cloud usage, optional scan packs |

Possible scan packs: $1.99/50 or $4.99/200. Do **not** promise unlimited cloud AI on a one-time plan. Previously indexed items remain readable and searchable after cancellation. Pricing and conversions are initial experiments, not validated findings.

## Delivery roadmap
- **Validation, weeks 1–2:** Interview 10–15 people, test the capture-to-find workflow, evaluate 50 real photos.
- **Core, weeks 3–5:** Camera, local DB, recognition review, confirmed placement, search.
- **Private beta, weeks 6–7:** Test with 20–30 households; measure retrieval accuracy and correction effort.
- **Launch, weeks 8–10:** Quotas, billing, privacy/delete/export, telemetry and AI cost control.

Follow-on: QR box labels; shared households; cloud sync; moving mode; visual bounding boxes; receipts/warranties (later). Keep user confirmation in the loop: occluded objects cannot be reliably recognized and moved items invalidate old photos.

## Data model
`Household`, `StorageLocation` (parent tree), `InventoryItem`, `ItemPlacement`, `InventoryPhoto`, `ItemDetection`, `ItemEvent`, `Subscription`, `LocatedItem` in `domain/model/InventoryModels.kt`. `ItemEvent` supports later movement history; `ItemPlacement` holds last confirmed placement. Dates are placeholder strings for now; replace with a proper cross-platform time type once persistence is integrated. Photos use local URI strings for now.

## Presentation architecture
```
commonMain/kotlin/org/techascent/aifinder/
  domain/model/               # immutable models
  domain/inventory/ location/ # repository interfaces, rules and use cases
  data/inventory/ location/   # Firebase + Room data sources and repositories
  presentation/
    components/               # small reusable Compose widgets
    theme/                    # feature-specific palette
    navigation/               # string-based route coordinator
    welcome/ home/ spaces/ scan/ review/ search/ details/
      <Screen>Screen.kt       # declarative view, LazyListScope
      <Screen>UiState.kt      # Loading/Success/Error
      <Screen>ViewModel.kt    # presentation state/actions only
```

ViewModels do not own the inventory rules. Authentication and location operations use injected use cases and repository interfaces. The location and inventory repositories combine Firebase access with Room local data sources. Navigation is currently minimal and has no deep links.

`AiFinderApp` provides a shared Compose `KoinApplication` using `di/aiFinderModule`. Welcome is the first injected screen: `WelcomeScreen` resolves its lifecycle-managed `WelcomeViewModel` with `koinViewModel()` and collects its read-only `state` StateFlow with `collectAsState()`. Retry is handled by the injected ViewModel. Other screens still construct their ViewModels in the mock navigation coordinator.

Uses `ComposaButton` from Composa; applies feature-local colors to avoid altering other Composa consumers. UI is shared between Android and iOS; no platform-specific UI dependencies have been introduced.

Home uses a search shortcut, scan prompt and cards for every space, ordered by full path. Nested space cards show their parent path, and newly created spaces appear through the Room cache observation without a manual refresh. Space cards open that space's items. See all opens every space. Bottom navigation is currently removed. Scan, Review, Search, Details, Spaces, Space and Account share the same Back button; Welcome and Home have none. Review retains its fixed Save action.

## UX and privacy
- Start with one drawer, not an entire home.
- Clearly distinguish AI suggestions, confirmed records, and manually entered items.
- Give users correction and deletion controls, opt-in cloud processing and export before production.
- Never hallucinate an item location when it has no confirmed placement.

## Product targets (not benchmarks)
First scan completion 70%+, first successful search 60%+, confirmed detection precision 90%+, indexed item retrieval success 90%+, local median search <1s, variable AI cost under $0.01/scan, paid conversion among activated users 3–5%.

## Validation limitations
The provided ZIP included local build outputs, but this workspace does not have the complete project Gradle wrapper caches / Android SDK / Apple Xcode environment configured. Android/iOS builds must be run on the developer machine. Do not treat the mock as a validated production implementation.
