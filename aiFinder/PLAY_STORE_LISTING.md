# AI Finder - Google Play listing and ASO plan

## Positioning

AI Finder is a photo-based home inventory and storage organizer: save what you
own, remember where you put it, and find its last confirmed location later.

Lead with the problem people want to solve, not the underlying AI technology.
Target people organizing drawers, cupboards, shelves, storage boxes, and moving
boxes.

No prompt, keyword selection, or description can guarantee a first-place search
ranking. Results vary by query, country, language, user, competition, and app
performance. This is a relevant, policy-conscious starting point, not a ranking
promise. Keyword demand and competition have not yet been measured.

## Ready-to-paste listing

Only copy the text inside each block into its corresponding Play Console field.

### App name - existing brand (30 characters; maximum 30)

```text
AI Finder: Where Did I Put It?
```

Keep this for launch to preserve the chosen app name. If discovery for home
inventory searches becomes the priority, consider `AI Finder: Home Inventory`
(25 characters) as a future title candidate. That is a recommendation, not an
app-name change made by this document.

### Short description (79 characters; maximum 80)

```text
Organize belongings with AI photos. Find saved items and where you stored them.
```

### Full description (maximum 4,000 characters)

```text
Where did you put that cable, passport, or spare key?

AI Finder is a home inventory and storage organizer that helps you remember where your belongings are. Photograph a drawer, shelf, or box, review the items suggested by AI, and save them to a place. Later, search your inventory to see an item's last confirmed location and source photo.

LESS SEARCHING. MORE FINDING.
Keep a record of the things you tuck away, from everyday accessories to belongings packed in storage boxes. Search saved names, categories, aliases, and place paths without opening every drawer.

BUILD YOUR INVENTORY FROM PHOTOS
Choose a place and take a photo. AI suggests names for visible objects, and you decide what to keep, correct, or add. Preview and retake your photo before analysis, then review the results before saving.

ORGANIZE ROOMS, SHELVES, AND BOXES
Create places that match your home: Bedroom > Wardrobe > Top drawer, or Garage > Shelf > Tool box. Use a simple list or nested places, whichever works for you.

FIND WHERE YOU LAST PUT IT
Search with an item name or a question such as "Where is my HDMI cable?" See its saved place path and reference photo, with object highlights when available. Search supports common synonyms and minor spelling mistakes.

KEEP YOUR RECORDS UP TO DATE
Add items manually, edit names and notes, move belongings between places, or confirm an item is still there. Reviewing a new scan lets you keep existing records or update matching items.

LABEL YOUR STORAGE
Generate printable, shareable QR labels for saved places. Scan a label inside AI Finder to open that place in your inventory. QR label scanning does not use AI scan credits.

ACCESS SAVED INVENTORY OFFLINE
Browse and search inventory already cached on your device without an internet connection. Sign in and refresh online to retrieve cloud changes, including updates from another device.

EXPORT YOUR INVENTORY
Share saved inventory details as a CSV spreadsheet or printable PDF. Exports contain inventory information, not photo files.

YOU STAY IN CONTROL
Manual place creation, manual item entry, and inventory search do not use scan credits. Photo analysis uses scan credits; check your balance in the app and purchase additional scan packs when available.

AI Finder remembers what you save. It does not track objects live or locate belongings you have not recorded. AI suggestions can be incorrect, so review them before saving. An internet connection is required for sign-in, AI analysis, cloud updates, and purchases.
```

## Search strategy - internal notes, not listing copy

| Intent | Candidate search phrases | Copy placement |
|---|---|---|
| Core organization | home inventory, storage organizer | Opening paragraph |
| Remembering locations | where did I put it, find saved items | App name, short description, opening |
| Physical storage | drawer organizer, storage boxes, moving boxes | Relevant use cases and visuals |
| Photo-based entry | photo inventory, AI item recognition | Photo feature explanation |
| Labeling and sharing | QR storage labels, inventory export | Supporting features |

These are hypotheses, not verified high-volume keywords. Do not paste this table
or a keyword block into the public description. Google Play has no separate
keyword field. Avoid competitor names, repetitive keyword variations, fabricated
reviews, and unsupported "best" or "#1" claims.

Do not target Bluetooth tracker, GPS finder, phone finder, or live object locator
searches: those imply capabilities this app does not provide.

## Visual messaging

Use actual app screens with clear, short captions in this order:

1. "Remember where you put it" - saved item with its real location path.
2. "Turn a photo into inventory" - photo preview and editable AI suggestions.
3. "A place for everything" - rooms and nested storage places.
4. "Search instead of rummaging" - a useful saved-item search result.
5. "Label boxes. Open their inventory." - generated QR label and place details.
6. "Keep your inventory handy" - item details or inventory export.

Use non-sensitive demonstration belongings and hide email addresses and other
personal information. Do not imply live tracking or guaranteed recognition.
Show the core benefit before billing or account screens.

## Launch and improvement plan

1. Select the most accurate category; Productivity is the starting recommendation.
   Choose only relevant tags actually available in Play Console.
2. Verify recognition, saving, search, QR labels, exports, offline cached access,
   and purchases against the exact release build before publishing this copy.
   Real recognition is currently enabled (`USE_FAKE_ANALYSIS = false`), but that
   setting alone does not establish that production credentials and services work.
3. Keep the existing app name initially. Use Play Console store listing experiments
   for supported fields such as short descriptions and screenshots, where
   available. Test one meaningful variable at a time; follow the reported
   confidence rather than choosing an early winner.
4. Track store listing visitors, acquisition/conversion, retained users, and
   available search-term reports. Measure relevant non-brand discovery, not just
   ranking for the app's own name. Avoid interpreting a small internal-test
   audience as evidence of public search performance.
5. Prioritize a successful first saved place and item. Fix crashes, slow flows,
   and failed scans; acquisition copy cannot compensate for poor retention.
6. Request honest reviews at an appropriate point after users experience value.
   Do not buy, reward, filter, or require positive ratings.
7. Localize the listing for markets the app can genuinely support. Translate
   meaning and search intent naturally; do not claim unsupported UI languages.
8. Revisit metadata using actual acquisition data. If trying the alternative
   title later, use a measured rollout and acknowledge that before/after results
   can be affected by seasonality and other changes.

## Publishing notes

- The three-free-scan offer is intentionally omitted from the main listing:
  eligibility depends on backend configuration and account creation time. If
  promoting it later, state the eligibility conditions accurately.
- Do not describe cached offline browsing as offline AI analysis or automatic
  realtime cross-device synchronization.
- Do not claim household collaboration, insurance valuation, theft recovery,
  unlimited scans, or complete inventory backups; those are not established
  features of this release.
- Confirm Data safety, privacy policy, account deletion requirements, and
  in-app-purchase disclosures separately. Marketing copy does not replace them.
- No app source code or app display names are changed by this document.

## Official references

- [Create and set up your app - store listing fields and limits](https://support.google.com/googleplay/android-developer/answer/9859152?hl=en)
- [Google Play metadata policy - accurate copy, title limits, no keyword spam](https://support.google.com/googleplay/android-developer/answer/9898842?hl=en)

Prepared November 12, 2026. Recheck current Play Console requirements when publishing.
