# Deploying AI Finder account functions

Deploy to the same Firebase project used by the Android and iOS apps. The project needs the **Blaze billing plan**. The function uses **Node.js 22**.

## Run the deployment script

First configure `aiFinder/firebase/functions/.env.YOUR_PROJECT_ID` as described in step 2 below. Then, from the repository root:

```bash
./aiFinder/firebase/deploy.sh YOUR_PROJECT_ID
```

The script validates the configuration, installs locked dependencies, runs unit tests, signs in to Firebase and deploys `ensureScanWallet` and `deleteProfile`. Configure the deletion secret below before running it. If your installed Node version is not 22 (including Node 23), it downloads/caches a temporary Node 22 through npm and uses it only for deployment; your global installation is unchanged. This requires access to the npm registry. It works regardless of your current directory when invoked by its path. It does not publish database rules: complete step 4 separately. Deployment can incur Firebase charges.

For project `pmscs-a2eed`, configure `functions/.env.pmscs-a2eed`, then run:

```bash
./aiFinder/firebase/deploy.sh pmscs-a2eed
```

Use `./aiFinder/firebase/deploy.sh --help` for usage. The manual steps below remain available.

## 1. Install dependencies and sign in

From the repository root:

```bash
cd aiFinder/firebase/functions
npm ci
npx firebase login
npx firebase projects:list
```

Find your Firebase **project ID**. Replace `YOUR_PROJECT_ID` in the following commands with that ID, not the project's display name.

## 2. Configure the function

Still in `aiFinder/firebase/functions`:

```bash
touch .env.YOUR_PROJECT_ID
```

Add the following to the new file (environment files, including `.env.example`, are intentionally git-ignored):

```dotenv
AIFINDER_DATABASE_URL=https://YOUR_ACTUAL_DATABASE_URL
WELCOME_SCANS_START_AT=2026-11-09T20:00:00.000Z
AIFINDER_STORAGE_BUCKET=YOUR_ACTUAL_BUCKET.firebasestorage.app
```

- Copy the exact database URL from **Firebase Console → Build → Realtime Database**. It must match the instance used by both apps.
- Choose the eligibility cutoff intentionally. Accounts created at or after this UTC instant receive three welcome scans once; older accounts retain their existing balance. The example is **21:00 Central European Time on November 9, 2026**.
- Do not move the cutoff backward later unless intentionally extending eligibility to older accounts.
- Set `AIFINDER_STORAGE_BUCKET` to the **exact** bucket used by Android's
  `FIREBASE_STORAGE_BUCKET` and iOS's Firebase configuration. A `gs://` prefix is
  accepted. Do not infer the bucket name from the project ID. The deletion
  function removes every object/version under `aifinder/users/<uid>/`, including
  orphan photos, and refuses to confirm success if any live objects remain.

### Configure the purchase-profile deletion secret

Create a RevenueCat **secret API key** for the same RevenueCat project as the app,
with permission to delete customers through the v1 subscriber API. Do not use the
public SDK key. Store it in Firebase Secret Manager, never in the app or git:

```bash
npx firebase functions:secrets:set REVENUECAT_SECRET_API_KEY --project YOUR_PROJECT_ID
```

Paste the key at the CLI prompt. The callable needs this even for accounts with
no purchases; a missing subscriber (HTTP 404) is accepted, other errors block
deletion and preserve the Firebase Auth account for retry. Deleting a RevenueCat
profile does not cancel store subscriptions or erase store financial records.

Project-specific `.env.*` files are git-ignored. Do not commit credentials or service-account keys.

## 3. Deploy the functions

From `aiFinder/firebase/functions`:

```bash
npx firebase deploy \
  --project YOUR_PROJECT_ID \
  --config ../firebase.json \
  --only functions:aifinder-welcome:ensureScanWallet,functions:aifinder-welcome:deleteProfile
```

Follow any API-enablement prompts. Your Google account needs permission to deploy functions and enable the required services. The function deploys to **us-central1**, matching both apps.

This command deploys only these two functions, not database rules. You do not need to run `firebase init`.

### Deletion prerequisites and verification

The function's runtime service account needs permission to list/delete objects
and versions in the configured bucket, update the configured Realtime Database,
delete Firebase Auth users, and access its bound secret. Use narrowly scoped IAM
permissions; a denied step must be fixed, not bypassed in the app.

Test with a disposable account, not a real user's inventory:

1. Sign in within the last 15 minutes. An older `auth_time` is rejected before
   server data deletion; sign out/sign in again, then retry.
2. Save an item/photo, a nested place, and feedback. Include an orphan image under
   this user's Storage prefix to verify that deletion does not depend on item
   references.
3. In Account, select Delete profile, acknowledge permanent data/credit loss, and
   confirm. Wait for Login; do not close the app while deleting.
4. Verify the RevenueCat customer, `aifinder/users/<uid>`,
   `aifinder/appReviews/<uid>`, all live/noncurrent Storage objects under
   `aifinder/users/<uid>/`, and the Firebase Auth user are removed. Check a second
   disposable account remains unchanged.
5. Test a denied Storage or RevenueCat operation in an isolated test project:
   the app must show failure, keep the Auth account, and support retry. A failure
   may occur after partial deletion; it must not be presented as a rollback.

Auth is deleted last. Firebase Auth deletion is project-wide: if other apps share
this same Firebase Auth account, their sign-in is affected too. Use a dedicated
Firebase project if that is not intended. The callable uses only its authenticated
UID, never a caller-supplied target UID.

Do not upload or edit inventory from another device while deletion runs.
Requests already in flight and cached copies on other devices cannot be remotely
erased by this operation. Existing ID tokens can remain valid until expiry under
Firebase's rules. Review production rules and operational deletion handling if
you need stronger concurrent-write isolation.

Storage soft-delete policies, retention locks and provider backups can retain
recoverable copies according to their configured retention periods; deleting
live objects does not override those policies. Verify bucket lifecycle settings
and keep privacy disclosures consistent. No user-facing restore is offered.

This deployment does not create the external account-deletion request page
required by Google Play. Maintain a public deletion-request URL and process
requests sent to `bdtechascent@gmail.com`.

## 4. Publish the database rules

In **Firebase Console → Realtime Database → Rules**, merge the contents of:

```text
aiFinder/firebase/welcome-grant.rules.json
```

into your existing rules, then select **Publish**.

**This is a rules fragment, not a complete replacement.** Preserve inventory, sign-in, purchase-history and feedback rules. Remove broad ancestor write permissions that would bypass the wallet restrictions.

The fragment allows owners to read their balance and preserves the existing client purchase/deduction flow, while preventing clients from creating, changing or deleting the welcome-grant marker. The function uses the Admin SDK to create that marker.

The existing client purchase flow is not trusted purchase verification. Before charging real users, move purchase crediting to a verified backend and tighten wallet rules as described in `aiFinder/README.md`.

## 5. Verify the grant

Reopen the app and tap **Retry** on Home's balance card.

For an eligible account without an earlier grant, inspect:

```text
aifinder/users/<FIREBASE_UID>/scanWallet
```

An account with no prior balance should have:

```json
{
  "remainingScans": 3,
  "activeSubscription": true,
  "welcomeGrant": {
    "version": 1,
    "scansGranted": 3,
    "grantedAt": 1794254400000
  }
}
```

The timestamp above is illustrative; the function records the actual server time. Existing credits are preserved and increased by three, rather than replaced.

Refresh again and sign in on another device: neither should grant another three scans. Accounts created before the configured cutoff should keep their existing balance.

If you already spent the welcome credits, a zero balance with the existing marker is correct; refreshing does not replenish them.

## Troubleshooting

From `aiFinder/firebase/functions`, inspect logs:

```bash
npx firebase functions:log \
  --project YOUR_PROJECT_ID \
  --only ensureScanWallet
```

If Home still cannot confirm the balance:

- Confirm the deployed project matches the app's Firebase configuration.
- Confirm `ensureScanWallet` is deployed in `us-central1`.
- Confirm `AIFINDER_DATABASE_URL` identifies the correct instance.
- Confirm `WELCOME_SCANS_START_AT` is a valid UTC ISO-8601 timestamp.
- Inspect function logs for permission or malformed-wallet errors.
- Confirm the app is signed in and has network access.

If the endpoint is unavailable, login still works, but remote scan-credit checks fail and analysis remains blocked. The app does not invent local credits.

## Local checks

From `aiFinder/firebase/functions`:

```bash
npm test
```

With Java 21 or newer available, also run:

```bash
npm run test:emulators
```

The emulator command uses the isolated `demo-aifinder-welcome` project, not your live project. It checks account eligibility, concurrent/repeated grants, preservation of purchased credits and welcome-marker protection.
