# Deploying the welcome-scan function

Deploy to the same Firebase project used by the Android and iOS apps. The project needs the **Blaze billing plan**. The function uses **Node.js 22**.

## Run the deployment script

First configure `aiFinder/firebase/functions/.env.YOUR_PROJECT_ID` as described in step 2 below. Then, from the repository root:

```bash
./aiFinder/firebase/deploy.sh YOUR_PROJECT_ID
```

The script validates the configuration, installs locked dependencies, runs unit tests, signs in to Firebase and deploys only `ensureScanWallet`. If your installed Node version is not 22 (including Node 23), it downloads/caches a temporary Node 22 through npm and uses it only for deployment; your global installation is unchanged. This requires access to the npm registry. It works regardless of your current directory when invoked by its path. It does not publish database rules: complete step 4 separately. Deployment can incur Firebase charges.

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
```

- Copy the exact database URL from **Firebase Console → Build → Realtime Database**. It must match the instance used by both apps.
- Choose the eligibility cutoff intentionally. Accounts created at or after this UTC instant receive three welcome scans once; older accounts retain their existing balance. The example is **21:00 Central European Time on November 9, 2026**.
- Do not move the cutoff backward later unless intentionally extending eligibility to older accounts.

Project-specific `.env.*` files are git-ignored. Do not commit credentials or service-account keys.

## 3. Deploy the function

From `aiFinder/firebase/functions`:

```bash
npx firebase deploy \
  --project YOUR_PROJECT_ID \
  --config ../firebase.json \
  --only functions:aifinder-welcome:ensureScanWallet
```

Follow any API-enablement prompts. Your Google account needs permission to deploy functions and enable the required services. The function deploys to **us-central1**, matching both apps.

This command deploys only the welcome function, not database rules. You do not need to run `firebase init`.

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
