#!/usr/bin/env bash
set -euo pipefail

usage() {
    printf 'Usage: %s FIREBASE_PROJECT_ID\n' "$0"
    printf 'Deploys ensureScanWallet and deleteProfile. Database rules must be merged separately.\n'
}

if [[ "${1:-}" == "--help" || "${1:-}" == "-h" ]]; then
    usage
    exit 0
fi

if [[ $# -ne 1 || ! "$1" =~ ^[a-z][a-z0-9-]{4,28}[a-z0-9]$ ]]; then
    usage >&2
    exit 1
fi

project_id="$1"
script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
cd "$script_dir/functions"

for tool in node npm; do
    if ! command -v "$tool" >/dev/null 2>&1; then
        printf 'Missing %s. Install Node.js 22 (including npm) first.\n' "$tool" >&2
        exit 1
    fi
done

if [[ "$(node -p 'process.versions.node.split(".")[0]')" != "22" ]]; then
    printf 'Current Node: %s. Preparing temporary Node.js 22; your global installation stays unchanged.\n' "$(node --version)"
    node_binary="$(npm exec --yes --package=node@22 -- node -p 'process.execPath')"
    if [[ ! -x "$node_binary" || "$("$node_binary" -p 'process.versions.node.split(".")[0]')" != "22" ]]; then
        printf 'Could not prepare Node.js 22. Check your npm registry connection.\n' >&2
        exit 1
    fi
    export PATH="$(dirname -- "$node_binary"):$PATH"
    exec bash "$script_dir/deploy.sh" "$project_id"
fi

env_file=".env.$project_id"
if [[ ! -f "$env_file" ]]; then
    printf 'Missing %s/functions/%s\n' "$script_dir" "$env_file" >&2
    printf 'Create that file with AIFINDER_DATABASE_URL, WELCOME_SCANS_START_AT and AIFINDER_STORAGE_BUCKET.\n' >&2
    printf 'See %s/DEPLOYMENT.md for instructions.\n' "$script_dir" >&2
    exit 1
fi

# Read dotenv as data, never execute it as shell code.
node --input-type=module - "$env_file" <<'NODE'
import { readFileSync } from "node:fs";
import { parseEnv } from "node:util";

const config = parseEnv(readFileSync(process.argv[2], "utf8"));
let url;
try {
    url = new URL(config.AIFINDER_DATABASE_URL);
} catch {
    console.error("Set AIFINDER_DATABASE_URL to your actual Firebase Realtime Database URL.");
    process.exit(1);
}
if (url.protocol !== "https:" ||
    !/^[a-z0-9-]+(?:\.[a-z0-9-]+)?\.(?:firebasedatabase\.app|firebaseio\.com)$/.test(url.hostname) ||
    url.username || url.password || url.port || url.search || url.hash || url.pathname !== "/") {
    console.error("AIFINDER_DATABASE_URL must be an HTTPS Firebase Realtime Database root URL.");
    process.exit(1);
}
const cutoff = config.WELCOME_SCANS_START_AT ?? "";
if (!/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(?:\.\d{3})?Z$/.test(cutoff) ||
    !Number.isFinite(Date.parse(cutoff))) {
    console.error("Set WELCOME_SCANS_START_AT to a valid UTC ISO-8601 timestamp ending in Z.");
    process.exit(1);
}
console.log(`Database: ${url.href}`);
console.log(`Eligible accounts created from: ${cutoff}`);
if (!/^(?:gs:\/\/)?[a-z0-9][a-z0-9._-]+(?:\/)?$/.test(config.AIFINDER_STORAGE_BUCKET ?? "")) {
    console.error("Set AIFINDER_STORAGE_BUCKET to the same bucket used by the app (gs:// prefix optional).");
    process.exit(1);
}
console.log(`Photo bucket: ${config.AIFINDER_STORAGE_BUCKET}`);
NODE

printf '\nDeploying ensureScanWallet and deleteProfile to project %s in us-central1.\n' "$project_id"
printf 'Database rules will NOT be deployed by this script.\n\n'
npm ci --no-audit --no-fund
npm test
npx --no-install firebase login
npx --no-install firebase deploy \
    --project "$project_id" \
    --config "$script_dir/firebase.json" \
    --only functions:aifinder-welcome:ensureScanWallet,functions:aifinder-welcome:deleteProfile

printf '\nFunction deployed. Merge/publish welcome-grant.rules.json separately, then retry the balance in the app.\n'
