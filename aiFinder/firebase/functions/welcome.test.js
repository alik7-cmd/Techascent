"use strict";

const { test } = require("node:test");
const assert = require("node:assert/strict");
const { initializeWallet, isEligible } = require("./welcome");

const now = Date.parse("2026-11-10T12:00:00Z");
const startAt = "2026-11-09T20:00:00Z";

test("eligibility uses Firebase account creation time, including the launch boundary", () => {
  assert.equal(isEligible("2026-11-09T19:59:59Z", startAt), false);
  assert.equal(isEligible(startAt, startAt), true);
  assert.equal(isEligible("2026-11-10T12:00:00Z", startAt), true);
  assert.throws(() => isEligible("invalid", startAt));
  assert.throws(() => isEligible(startAt, "invalid"));
});

test("new eligible wallet gets exactly three scans and a durable grant", () => {
  assert.deepEqual(initializeWallet(null, true, now), {
    remainingScans: 3,
    activeSubscription: true,
    welcomeGrant: { version: 1, scansGranted: 3, grantedAt: now },
  });
});

test("retries, later logins and transaction retries never grant twice", () => {
  const first = initializeWallet(null, true, now);
  assert.deepEqual(initializeWallet(first, true, now + 1000), first);
  const spent = { ...first, remainingScans: 0, usedScans: { a: true, b: true, c: true } };
  const refreshed = initializeWallet(spent, true, now + 2000);
  assert.equal(refreshed.remainingScans, 0);
  assert.equal(refreshed.activeSubscription, false);
  assert.deepEqual(refreshed.usedScans, spent.usedScans);
  assert.deepEqual(refreshed.welcomeGrant, first.welcomeGrant);
});

test("existing accounts without wallets get zero, not a welcome grant", () => {
  assert.deepEqual(initializeWallet(null, false, now), {
    remainingScans: 0, activeSubscription: false,
  });
});

test("purchases credited before initialization and unrelated wallet fields are preserved", () => {
  const purchased = {
    remainingScans: 5, activeSubscription: false,
    creditedPurchases: { transaction: true }, usedScans: { earlier: true },
  };
  const initialized = initializeWallet(purchased, true, now);
  assert.equal(initialized.remainingScans, 8);
  assert.equal(initialized.activeSubscription, true);
  assert.deepEqual(initialized.creditedPurchases, purchased.creditedPurchases);
  assert.deepEqual(initialized.usedScans, purchased.usedScans);
  assert.equal(purchased.remainingScans, 5);
  assert.equal(purchased.welcomeGrant, undefined);
});

test("existing balances remain unchanged and stale active flags are repaired", () => {
  assert.deepEqual(initializeWallet({ remainingScans: 7, activeSubscription: false }, false, now),
    { remainingScans: 7, activeSubscription: true });
  assert.deepEqual(initializeWallet({ remainingScans: 0, activeSubscription: true }, false, now),
    { remainingScans: 0, activeSubscription: false });
});

test("legacy missing balance is initialized without deleting other fields", () => {
  assert.deepEqual(initializeWallet({ usedScans: { photo: true } }, false, now),
    { usedScans: { photo: true }, remainingScans: 0, activeSubscription: false });
});

test("malformed balances and grant markers fail instead of resetting or granting", () => {
  for (const remainingScans of [-1, 1.5, "3", true, Number.MAX_SAFE_INTEGER + 1]) {
    assert.throws(() => initializeWallet({ remainingScans }, true, now));
  }
  for (const wallet of [3, "wallet", [], { welcomeGrant: false }, { welcomeGrant: {} }]) {
    assert.throws(() => initializeWallet(wallet, true, now));
  }
  assert.throws(() => initializeWallet({ remainingScans: Number.MAX_SAFE_INTEGER }, true, now));
});
