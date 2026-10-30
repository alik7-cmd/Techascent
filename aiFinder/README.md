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

**Current implementation is a UI-only mock.** No camera, image processing, local database, authentication, payments, cloud sync, API integration or real item mutations exist yet. Scan and review use mock objects. Search is a keyword-based demo, not semantic search. Screens provide `Loading`, `Success` and `Error` UI states, but the mock repository returns synchronously and currently starts in Success.

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
  data/                       # InventoryRepository + mock implementation
  presentation/
    components/               # small reusable Compose widgets
    theme/                    # feature-specific palette
    navigation/               # temporary mock route coordinator
    welcome/ home/ scan/ review/ search/ details/
      <Screen>Screen.kt       # declarative view, LazyListScope
      <Screen>UiState.kt      # Loading/Success/Error
      <Screen>ViewModel.kt    # presentation state/actions only
```

ViewModels do not own the inventory rules. A future use-case layer can be injected into ViewModels and the mock repository replaced with SQLDelight/Room, photo analysis and a sync implementation. Navigation is currently minimal and has no back stack or deep links; replace it when adopting the app's production navigation solution.

`AiFinderApp` provides a shared Compose `KoinApplication` using `di/aiFinderModule`. Welcome is the first injected screen: `WelcomeScreen` resolves its lifecycle-managed `WelcomeViewModel` with `koinViewModel()` and collects its read-only `state` StateFlow with `collectAsState()`. Retry is handled by the injected ViewModel. Other screens still construct their ViewModels in the mock navigation coordinator.

Uses `ComposaButton` from Composa; applies feature-local colors to avoid altering other Composa consumers. UI is shared between Android and iOS; no platform-specific UI dependencies have been introduced.

Home uses a search shortcut, scan prompt and room summary cards. See all scrolls to the full spaces list. Search and Scan use the existing mock routes, and room cards currently open Search. Bottom navigation is currently removed. Scan, Review, Search and Details share a text-style Back button; Welcome and Home have none. Review retains its fixed Save action. Space/box and item counts are illustrative mock summary metadata, not totals derived from the small searchable fixture.

## UX and privacy
- Start with one drawer, not an entire home.
- Clearly distinguish AI suggestions, confirmed records, and manually entered items.
- Give users correction and deletion controls, opt-in cloud processing and export before production.
- Never hallucinate an item location when it has no confirmed placement.

## Product targets (not benchmarks)
First scan completion 70%+, first successful search 60%+, confirmed detection precision 90%+, indexed item retrieval success 90%+, local median search <1s, variable AI cost under $0.01/scan, paid conversion among activated users 3–5%.

## Validation limitations
The provided ZIP included local build outputs, but this workspace does not have the complete project Gradle wrapper caches / Android SDK / Apple Xcode environment configured. Android/iOS builds must be run on the developer machine. Do not treat the mock as a validated production implementation.
