"use strict";

const { test } = require("node:test");
const assert = require("node:assert/strict");

const emulatorsAvailable = Boolean(
  process.env.FIREBASE_AUTH_EMULATOR_HOST && process.env.FIREBASE_DATABASE_EMULATOR_HOST,
);

test("authenticated welcome grants, concurrent retries and client rules", {
  skip: !emulatorsAvailable,
}, async () => {
  const projectId = "demo-aifinder-welcome";
  process.env.GCLOUD_PROJECT = projectId;
  process.env.AIFINDER_DATABASE_URL = `https://${projectId}-default-rtdb.firebaseio.com`;
  process.env.WELCOME_SCANS_START_AT = "2026-11-09T20:00:00Z";
  const { getApp, deleteApp, initializeApp } = require("firebase-admin/app");
  const { getAuth } = require("firebase-admin/auth");
  const { getDatabaseWithUrl } = require("firebase-admin/database");
  const { ensureScanWallet } = require("./index");
  const app = getApp();
  const auth = getAuth(app);
  const db = getDatabaseWithUrl(process.env.AIFINDER_DATABASE_URL, app);
  const userPath = (uid) => `aifinder/users/${uid}/scanWallet`;
  const call = (uid, data = {}) => ensureScanWallet.run({ auth: { uid }, data });
  let clientApp;
  try {
    await db.ref().remove();
    for (const [uid, creationTime] of [
      ["new-user", "2026-11-10T00:00:00Z"],
      ["existing-user", "2026-11-08T00:00:00Z"],
      ["purchase-first", "2026-11-10T00:00:00Z"],
    ]) {
      await auth.importUsers([{
        uid, email: `${uid}@example.test`,
        metadata: { creationTime },
        providerData: [{ uid, providerId: "google.com", email: `${uid}@example.test` }],
      }]);
    }

    await assert.rejects(ensureScanWallet.run({ data: {} }), { code: "unauthenticated" });
    const responses = await Promise.all(Array.from({ length: 8 }, () => call("new-user")));
    assert.ok(responses.every((response) => response.remainingScans === 3));
    const first = (await db.ref(userPath("new-user")).get()).val();
    assert.equal(first.welcomeGrant.scansGranted, 3);

    await db.ref(userPath("new-user")).update({ remainingScans: 0, activeSubscription: false });
    assert.equal((await call("new-user")).remainingScans, 0);
    assert.deepEqual((await db.ref(userPath("new-user")).get()).val().welcomeGrant, first.welcomeGrant);
    assert.equal((await call("existing-user", { uid: "new-user" })).remainingScans, 0);

    await db.ref(userPath("purchase-first")).set({
      remainingScans: 5, activeSubscription: true, creditedPurchases: { purchase: true },
    });
    assert.equal((await call("purchase-first")).remainingScans, 8);
    assert.equal((await db.ref(userPath("purchase-first")).get())
      .child("creditedPurchases/purchase").val(), true);

    clientApp = initializeApp({
      projectId,
      databaseURL: process.env.AIFINDER_DATABASE_URL,
      databaseAuthVariableOverride: { uid: "purchase-first" },
    }, "wallet-owner");
    const client = getDatabaseWithUrl(process.env.AIFINDER_DATABASE_URL, clientApp);
    const walletRef = client.ref(userPath("purchase-first"));
    const current = (await walletRef.get()).val();
    assert.equal(current.remainingScans, 8);
    await walletRef.set({ ...current, remainingScans: 7 });
    await assert.rejects(walletRef.update({ "welcomeGrant/scansGranted": 30 }));
    await assert.rejects(walletRef.update({ "welcomeGrant/extra": true }));
    await assert.rejects(walletRef.child("welcomeGrant").remove());
    await assert.rejects(walletRef.remove());
    await assert.rejects(client.ref(userPath("new-user")).get());
    await assert.rejects(client.ref(userPath("existing-user")).update({
      welcomeGrant: { version: 1, scansGranted: 3, grantedAt: Date.now() },
    }));
    // The existing purchase flow still credits balances from the client.
    await walletRef.set({ ...current, remainingScans: 10 });
  } finally {
    if (clientApp) await deleteApp(clientApp);
    await deleteApp(app);
  }
});
