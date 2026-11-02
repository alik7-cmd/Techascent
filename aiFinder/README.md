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

**Current implementation:** camera capture, editable scan review, Android Google/Firebase authentication, Gemini (Vertex AI) photo analysis, and user-scoped Firebase locations and items with a persistent Room cache. Home, spaces, search and item details read from that cache. iOS authentication and Firebase location/item access still need native integration.

## Location cache
Room Multiplatform stores locations in `aifinder.db` on Android and iOS, using a shared entity/DAO and bundled SQLite driver. Rows are keyed by Firebase user ID and location ID. The repository reads Firebase only when that user's local cache is empty; subsequent reads observe Room. Creating a location writes Firebase first and then upserts Room. Remote failures are surfaced without inserting a local-only location.

Room adopts the existing version-1 SQLite cache without deleting its records. KSP generates the platform database constructors; exported Room schemas live in `aiFinder/schemas`. Commit schema changes alongside future migrations.

This is offline-first, not live cross-device synchronization:
- Locations and items load from Room, with no network call, whenever the cache has data.
- When the cache is empty, Firebase is read with a 10-second timeout. If that read fails or times out (for example offline), the app shows the empty cache instead of an error and tries Firebase again the next time it observes.
- Changes from other devices are not fetched while the cache has data.

Home shows a shimmer skeleton of the spaces list until the cache emits.

## Inventory cache
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
Welcome checks the restored Firebase session and a device-local Room welcome flag while showing only the background (no content or spinner). Signed-in users go directly to Home. Signed-out users see welcome content once per app data installation; subsequent launches go directly to Login. The flag is saved before the first welcome content is shown. Continuing replaces Welcome with Login, and Login has no route back to Welcome. Room migration 2-to-3 adds the flag table without changing cached locations.

## Account settings
The account icon on Home opens `AccountScreen`. It shows the signed-in Firebase user's name, email, initial and sign-in provider (Google or Apple). The Subscription, Household & sharing and Privacy & data cells are placeholders for now. Log out asks for confirmation in a native dialog (Material `AlertDialog` on Android, `UIAlertController` on iOS). It then calls `SignOutUseCase`, clears the location state and returns to Login.

## Monetization proposal (hypotheses to validate)
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

Home uses a search shortcut, scan prompt and top-level space cards. Space cards open that space's items. See all opens every space. Bottom navigation is currently removed. Scan, Review, Search, Details, Spaces, Space and Account share the same Back button; Welcome and Home have none. Review retains its fixed Save action.

## UX and privacy
- Start with one drawer, not an entire home.
- Clearly distinguish AI suggestions, confirmed records, and manually entered items.
- Give users correction and deletion controls, opt-in cloud processing and export before production.
- Never hallucinate an item location when it has no confirmed placement.

## Product targets (not benchmarks)
First scan completion 70%+, first successful search 60%+, confirmed detection precision 90%+, indexed item retrieval success 90%+, local median search <1s, variable AI cost under $0.01/scan, paid conversion among activated users 3–5%.

## Validation limitations
The provided ZIP included local build outputs, but this workspace does not have the complete project Gradle wrapper caches / Android SDK / Apple Xcode environment configured. Android/iOS builds must be run on the developer machine. Do not treat the mock as a validated production implementation.
