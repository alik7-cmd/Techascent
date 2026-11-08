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

**Current implementation:** preview/retake capture, editable and duplicate-aware scan review, Google/Firebase authentication on Android and iOS, Gemini (Vertex AI) recognition, and user-scoped Firebase inventory backed by Room. Both platforms support item mutations, explicit refresh, scan photos, transactional scan credits, QR labels and local decoding, and CSV/PDF sharing. Household invitations remain deferred.

## Location cache
When Home has no places, its section action is **Add place** instead of See all. Place creation and manual inventory entry are free, without a wallet check or paywall. All places also provides Add place. Home puts its compact search shortcut first, with a subtle teal outline and sparkle accent; scan credits are secondary.

Room Multiplatform stores locations in `aifinder.db` on Android and iOS, using a shared entity/DAO and bundled SQLite driver. Rows are keyed by Firebase user ID and location ID. The repository reads Firebase only when that user's local cache is empty; subsequent reads observe Room. Creating a location writes Firebase first and then upserts Room. Remote failures are surfaced without inserting a local-only location.

Room adopts the existing version-1 SQLite cache without deleting its records. KSP generates the platform database constructors; exported Room schemas live in `aiFinder/schemas`. Commit schema changes alongside future migrations.

This is offline-first, not live cross-device synchronization:
- Locations and items load from Room, with no network call, whenever the cache has data.
- When the cache is empty, Firebase is read with a 10-second timeout. If that read fails or times out (for example offline), the app shows the empty cache instead of an error and tries Firebase again the next time it observes.
- Home refreshes locations and items on entry and offers **Refresh inventory**. A failed refresh retains cached content and displays an error. This is explicit snapshot synchronization, not realtime or background synchronization.

Home shows a shimmer skeleton of the spaces list until the cache emits.

Place details show a mint location summary, an optional representative scan photo, nested item paths, manual Add item, a QR label and a scan action. Item details support Edit, Move, confirmed Delete and **Still here**. Mutations write Firebase before Room and surface failures without pretending a local-only write succeeded. Confirmation dates are stored independently from original creation dates. Search results show photo/path/freshness immediately and provide Still here, Move and Details actions.

## Inventory cache
Scanning starts with a prominent **Choose a place first** card and a direct selection action, rather than a disabled capture button with a small warning. Choosing/creating a place is free. **Open camera** opens a dedicated capture route with a large camera surface and fixed actions. The preview fits the entire image without cropping, including during analysis; **Retake** is free. **Analyze** runs the existing authoritative credit check before recognition. An animated local AI sparkle/sweep and shimmer feedback indicate activity without invented progress percentages or fake item markers. Place selection carries over to review. Back from capture cancels pending capture/analysis and returns to place setup.

The place picker shows the current selection, uses shimmer loading, blocks creation while places are loading or unavailable, and resets stale breadcrumbs when a browsed place disappears. Home groups **Refresh inventory** and **Scan label** as matching outlined icon chips that wrap on smaller screens. Refresh retains its disabled busy state and error feedback.

AI Finder uses an app-scoped light Material theme with teal controls, cursors and text selection, including sheets and Android dialogs, independent of the system appearance. This does not change other apps using Composa. Item details distinguish outlined source-photo removal (with a shared-photo warning) from tonal destructive item deletion; both still require confirmation. Detail photos fill the content width at their original aspect ratio without cropping or stretching; portrait photos scroll with the page.

Last known location is a numbered path with downward arrows and “Go to / Then look inside” guidance. Search results group Still here, Move item and Details into wrapping icon chips. Home and Scan use higher-contrast guidance, a camera icon for photo scanning, and a directional affordance for choosing or changing a place.

Refresh failures appear in a compact, accessible error card matching sign-in feedback, with a dismiss button; the refresh chip becomes Try again while cached inventory stays visible. Item editing and moving use a read-only exposed dropdown with a disclosure indicator, full place paths and loading/empty guidance. Place QR label is a secondary rounded outlined button with a subtle mint tint and QR icon.

Review refreshes saved metadata once before checking for rescan matches; a failed check blocks saving with a retry action. This adds no AI call. Rescans match exact case-insensitive names in the chosen place. Existing items default to **Keep unchanged**; users can explicitly update an existing item's photo/confirmation date or add a separate item. Ambiguous same-name matches cannot overwrite an arbitrary record. Updating preserves notes, aliases, original ID and creation date. Items missing from a new photo are never automatically removed. Stable scan/photo-item IDs and manual draft IDs make uncertain write retries idempotent.

The place picker browses one level at a time, with tappable breadcrumbs, full-path search results and explicit `Use [place]` / `Add a place inside [place]` actions. It opens at All places rather than silently using the previous selection as a new place's parent. Adding requires only a name and shows the containing place and resulting path; standalone places use the existing ROOM type and nested places use CONTAINER internally. Existing types and relationships remain unchanged, including legacy Home roots. A `See how it works` guide illustrates Bedroom → Wardrobe → Top drawer and explains that nesting is optional.

### Development: fake analysis
Set `AppConfig.USE_FAKE_ANALYSIS` in `aiFinder/src/commonMain/kotlin/org/techascent/aifinder/config/AppConfig.kt`, then rebuild and restart the app. It is currently `true`: Android and iOS return sample HDMI cable, passport and key detections without creating the AI HTTP client or calling Vertex AI, so analysis incurs no Google Cloud AI charges and needs no Vertex API key. Samples ignore the photo and omit bounding boxes rather than inventing positions. Set it to `false` before shipping to restore real analysis.

Only recognition is faked. Authentication, location selection, scan-credit checks/deductions, and saving photos/items still use their existing implementations and may make Firebase calls. Successful fake scans still cost one app scan credit. There are no account-based credit bypasses. Saving sample results persists sample inventory and the captured photo normally.

The Home/Scan credit card prepares purchases inline: Get more scans becomes a spinner while access and store identity are checked. Preparation failures stay on the card; leaving the screen cancels preparation. Only the ready paywall opens a modal. Capturing a photo is local; `MeteredPhotoAnalyzer` must successfully refresh remote access before invoking either analyzer. Zero credits or an access-check failure never calls Vertex AI. Purchase completion resumes the pending photo through that same access gate, not directly through the AI implementation.

Scans are analyzed by `VertexGeminiPhotoAnalyzer` using `VERTEX_API_KEY` and `VERTEX_MODEL` from the git-ignored root `local.properties`. One recognition request returns names, categories and optional normalized bounding boxes. Photos are oriented, JPEG-compressed and capped at a 1,280-pixel longest side; responses are capped at 15 objects and 2,048 output tokens. Thinking is disabled for Gemini 2.5 Flash variants. Review/search/details draw pointers locally from those same boxes: displaying or selecting a pointer makes **no extra AI call**. Missing boxes are not fabricated. These limits bound each request, not aggregate provider billing.

Reviewed items are saved to `aifinder/users/{uid}/items/{itemId}` in one atomic Realtime Database update, then upserted into Room. Records include name, category, tags/aliases, notes, locationId, photoId, confidence, createdAt, confirmedAt and optional bounds. A failed save keeps review results and shows an error.

`ObserveInventoryUseCase` combines cached locations and items into an `InventorySnapshot`, which provides:
- Home's top-level spaces, with descendant space and item counts.
- See all, listing every location ordered by path.
- Space details, showing items in a location and its nested locations.
- Ranked local search over names, categories, aliases and place paths. Question words are ignored; common synonyms and conservative one-edit typos are supported. Numeric identifiers are not fuzzy-matched, and all meaningful query words must match. Search makes no AI calls.

Room migration 3-to-4 adds `CachedItem`; 5-to-6 adds notes, confirmation dates and bounds, initializing legacy confirmation dates from creation dates. Exported schema 6 and migration coverage preserve existing records. Sign-out resets inventory ViewModels and clears the signed-out user's existing local caches through the sign-out use case.

## Photo storage

Each scan photo is uploaded to Firebase Storage at `aifinder/users/{uid}/photos/{photoId}.jpg`. The bucket is set by `FIREBASE_STORAGE_BUCKET` in `local.properties`, e.g. `gs://pmscs-a2eed.firebasestorage.app`. Items are saved only after the upload succeeds.

The photo is also cached per user in Android app files and iOS Application Support. Screens read the cache first and download only on a miss. Byte and decoded-image caches are bounded and user-scoped. The native iOS FirebaseStorage adapter handles upload, download and deletion.

**Delete source photo** in item details explicitly confirms its shared scope: every item using that scan loses the photo, but item names and placements remain. Cloud deletion is retry-safe; matching item references/bounds and this device's caches are cleared. A failed step is surfaced. Other devices can retain previously cached photo files until their local app data is cleared; this is not remote device erasure.

### QR labels and exports
A place's QR sheet generates a printable/shareable PNG with its human-readable path and an `aifinder://place/{id}` payload. Home's **Scan label** captures and decodes once on-device (ZXing on Android, CoreImage on iOS) and opens only a place in the current account's inventory. Invalid, foreign-account and unreadable labels show an error. Scanning labels uses neither AI nor scan credits. Labels are scanned inside the app; external OS deep-link handling is not configured.

Account has a dedicated **Export inventory** entry with separate icon cards for CSV spreadsheets and printable PDF documents, format guidance, progress and visible errors. **Privacy & data** is a separate plain-language screen about managing items and sharing thoughtfully, without infrastructure details. CSV quotes fields and neutralizes spreadsheet formulas; PDF paginates locally. Exports contain cached metadata, not photo files; refresh Home first for other-device changes. Files are shared via Android's non-exported, cache-only FileProvider or iOS's native share sheet. Sharing an export reveals its contents to recipients.

You need Storage rules that allow `aifinder/users/{uid}/**` when `request.auth.uid == uid`.

## Startup routing
### App icon
Android and iOS share a deep-teal storage-drawer and magnifying-glass mark, with AI sparkles inside the lens and mint scan brackets. Android includes adaptive and Android 13 themed icons, plus density-specific legacy icons. iOS includes opaque iPhone, iPad and App Store assets. Regenerate the artwork with `python3 aiFinder/tools/generate_app_icon.py` (requires Pillow); the generator owns the shared geometry, colors, raster exports and Android foreground vectors.

Welcome and the first Login slide share `WelcomeIllustration`, a Compose Canvas drawing of the drawer, stored objects, storage box and magnifying glass. It has no embedded background or bitmap dependency; its surface, accent, outline and inset colors can be supplied by a future theme. The surrounding mint panel remains owned by each screen.

Welcome checks the restored Firebase session and a device-local Room welcome flag while showing only the background (no content or spinner). Signed-in users go directly to Home. Signed-out users see welcome content once per app data installation; subsequent launches go directly to Login. The flag is saved before the first welcome content is shown. Continuing replaces Welcome with Login, and Login has no route back to Welcome. Room migration 2-to-3 adds the flag table without changing cached locations.

## Navigation
### Development credit-card comparison
Home uses the compact `ScanBalanceCardV3`, with the Deep Teal gradient, real balance and inline Get more/retry actions. `AppConfig.SHOW_COMPARISON` controls an optional comparison below Home's real card; it is currently disabled. Enable it to compare Deep Teal, Soft Mint and Compact Wallet with sample available, zero and loading balances. Preview purchase buttons do nothing.

`AppConfig.HOME_LAYOUT` selects `HomeLayout.POLISHED` (default) or `HomeLayout.DASHBOARD`; rebuild and restart after changing it. Both keep search first, grouped refresh/QR actions, live place cards and the credit wallet below inventory content. Polished Home has a shorter photo-scan prompt and clearer place-card arrows. Dashboard adds real inventory totals, Scan/Add place/All places quick actions and the three most recently created items, each opening its details. Totals use the complete cached inventory, not the three preview cards or summed subtree counts. Loading uses shimmer, and failures retain the existing retry UI; no extra AI requests are introduced.

All back-enabled screens use the shared `ScreenTopBar`: a fixed back chevron and left-aligned title with safe-area padding and an accessible heading. Account, Search, Scan, Review, Spaces, Space and Item Details share the same sizing and spacing; titles are not duplicated in the scrolling content.

`AiFinderNavigation` uses Navigation Compose's multiplatform `NavHost`, with all routes defined in the sealed `AiFinderScreen` class. Space and item details receive encoded IDs as navigation arguments. Back returns to the actual originating screen, including when opening Scan from a space. Welcome completion, sign-in and sign-out replace the back stack, preventing Back from reopening authentication screens or a signed-out session. Saving a review returns to Home and removes the scan/review flow from the stack. Shared ViewModels retain the scan-to-review handoff and existing logout cleanup.

## Account settings
Account's Purchase history row opens a read-only list of the signed-in user's Firebase `aifinder/users/{uid}/purchases` records, newest first. It shows the scan pack, local purchase date and crediting status, with shimmer loading, empty and retry states. Android reads Firebase directly; iOS uses the native Firebase bridge. This list reflects recorded scan-pack purchases, not a recurring subscription or a full store billing/refund ledger; prices are not invented. Firebase rules must allow the owner to read their purchases. Home retains its Get more purchase flow.

After authentication, Android and iOS update `aifinder/users/{uid}/signedIn` with `platform` (`ANDROID` or `IOS`), `provider` (`Google` or `Apple`), and `deviceName`. This stores the latest sign-in, not a history, and leaves locations/items untouched. The write is best-effort with a 15-second timeout: failures are logged without blocking successful login. Realtime Database rules must permit writes to this node only when `auth.uid == uid`. Restored sessions do not trigger another write.

Device names require no permissions: Android uses manufacturer/model and iOS uses `UIDevice.name` (often a generic name such as “iPhone”).

The account icon on Home opens `AccountScreen`, with the signed-in user's profile, Purchase history and Privacy & data. The inactive Household & sharing row is hidden until invitations are implemented. Log out uses a native confirmation dialog, calls `SignOutUseCase`, resets shared screen/action state and replaces the back stack with Login.

## Scan credit purchases
Get more prepares the RevenueCat paywall inline before opening it. Account remains a purchase-history entry point, not a purchase gate for free organization. Scan analysis always uses the same metered analyzer; there are no privileged-user credit bypasses.

## RevenueCat purchases
- **UI:** `SubscriptionSheet` shows RevenueCat's dashboard-designed `Paywall` full screen only after purchase preparation and user identification succeed. Loading, crediting, errors, missing configuration and delayed credits use an app-styled status dialog with retry/cancel where appropriate.
- **SDK:** `purchases-kmp-core` and `purchases-kmp-ui` `4.0.0`, using Kotlin 2.3.20 and Compose Multiplatform 1.9.3.
- **Keys:** add `REVENUECAT_ANDROID_API_KEY` and `REVENUECAT_IOS_API_KEY` to the git-ignored root `local.properties`. A RevenueCat **Test Store** (sandbox) public key works for development without Play/App Store setup. A blank key disables purchases on that platform; the app keeps working and shows the preview sheet. The keys are generated into `config/RevenueCatConfig`. Use public SDK keys only; never use secret keys.
- **Dashboard setup:** create consumable products `scan_pack_3` and `scan_pack_5`, add them as packages to the **current offering**, and attach a paywall to it. No entitlement is needed, because credits live in Firebase.
- **Identity:** `RevenueCatPurchaseGateway` configures the SDK lazily, with the Firebase UID as `appUserId`. Login calls `logIn(uid)` (best-effort), and logout calls `logOut()`. The paywall is shown only after the user is identified, so a purchase is never attached to an anonymous RevenueCat ID.
- **After purchase:** the app records the transaction and credits the wallet, falling back to polling (8 × 2 s) if recording fails. The sheet shows "Adding your scans…" while crediting. A pending analysis automatically resumes using the same captured photo once access is confirmed; no second capture or Analyze tap is needed. Late credits show a retryable pending state. Dismissing purchases or leaving Scan discards the pending photo.
- **Granting credits (client):** when the paywall reports `onPurchaseCompleted`, the app writes the `StoreTransaction` to `aifinder/users/{uid}/purchases/{transactionId}` (`purchaseId`, `productId`, `scansGranted`, `purchasedAt`, `store`, `status` `pending` → `credited`) and, in one Firebase transaction, adds 3 (`scan_pack_3`) or 5 (`scan_pack_5`) to `scanWallet/remainingScans`, sets `activeSubscription: true`, and marks `scanWallet/creditedPurchases/{transactionId}` so a purchase is never credited twice. Invalid key characters in transaction IDs are replaced with `_`. If the write fails, the app polls the wallet in case a backend credits it. Because the client can write its own wallet, a RevenueCat webhook plus tighter database rules is still recommended before production.
- **iOS:** RevenueCat 4 includes its native implementation; do not add `PurchasesHybridCommon` or `PurchasesHybridCommonUI` through SPM/CocoaPods. The iOS deployment target is 15+. The native Firebase bridge implements wallet loads, idempotent scan deductions and purchase credits with database transactions.

### Running the separate iOS app

Open `aiFinderIos/aiFinderIos.xcodeproj` from the repository root and select the `aiFinderIos` scheme. The build phase runs `:aiFinder:embedAndSignAppleFrameworkForXcode` and the SwiftUI host launches `AiFinderApp` through `MainViewController`. Java 17 is required; the script discovers it with `/usr/libexec/java_home` when `JAVA_HOME` is unset. For a physical device, select your signing team in Xcode.

The existing `iosApp` project still runs `composeApp` and is unchanged. Xcode merges generated metadata with `aiFinderIos/aiFinderIos/Info.plist`, including the Boolean `CADisableMinimumFrameDurationOnPhone = YES` required by Compose. Preserve it and the camera permission description. The host installs auth, purchase-history, inventory/photo/wallet and local QR/export bridges before creating the Compose view.

### iOS Google sign-in

The Swift host links FirebaseCore, FirebaseAuth, FirebaseDatabase, FirebaseStorage and GoogleSignIn through Swift Package Manager. It installs `FirebaseAuthBridge` before creating Compose, then forwards `.onOpenURL` to Google Sign-In. Kotlin adapters preserve cancellation and shared error handling. Firebase's persisted user drives startup routing; logout clears local caches and signs out of Firebase and Google. No OAuth tokens are stored by the bridge.

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
- **Scan entry:** Home/Places scan actions open Scan without an access gate. Users can select a place, capture and retake regardless of credits. Analyze checks authoritative access before AI. Insufficient credits retain the preview in memory for purchase resumption; connection/permission failures surface as errors, not false zero-credit balances. Browsing remains offline-first; checking and spending credits requires Firebase access.
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

Inventory rules live in the domain layer; lifecycle-managed ViewModels use injected repositories/use cases. Firebase-backed repositories read through Room. Navigation Compose handles the routes and encoded arguments; external deep links are not configured.

`AiFinderApp` provides a shared Compose `KoinApplication` using `di/aiFinderModule`. Screens and the navigation coordinator obtain lifecycle-managed ViewModels with `koinViewModel()` and collect their state. The coordinator shares scan/review/action instances and resets user-specific state on logout.

Shared UI uses `ComposaButton` and feature-local colors. Camera previews, authentication, confirmation dialogs and file-sharing use native platform adapters.

Home uses a search shortcut, scan prompt and cards for every space, ordered by full path. Nested space cards show their parent path, and newly created spaces appear through the Room cache observation without a manual refresh. Space cards open that space's items. See all opens every space. Bottom navigation is currently removed. Scan, Review, Search, Details, Spaces, Space and Account share the same Back button; Welcome and Home have none. Review retains its fixed Save action.

## UX and privacy
- Start with one drawer, not an entire home.
- Clearly distinguish AI suggestions, confirmed records, and manually entered items.
- Keep cloud analysis behind an explicit Analyze action, with correction, deletion and export controls.
- Never hallucinate an item location when it has no confirmed placement.

## Product targets (not benchmarks)
First scan completion 70%+, first successful search 60%+, confirmed detection precision 90%+, indexed item retrieval success 90%+, local median search <1s, variable AI cost under $0.01/scan, paid conversion among activated users 3–5%.

## Validation and release limitations
Use `:aiFinder:testDebugUnitTest`, `:aiFinder:assembleDebug` and `:aiFinder:compileKotlinIosSimulatorArm64`, plus the native Xcode build, to validate shared and bridge changes. Unit tests cover local search, persistence/migration, retry-safe writes, rescan choices, QR round trips, exports and capture/credit separation. Native compilation is not an end-to-end verification of live Firebase rules, store purchases or iOS device camera behavior. Fake analysis is still enabled for development; turn it off for real recognition. A trusted purchase-verification backend remains necessary before production.
