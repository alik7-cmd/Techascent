"use strict";

const WELCOME_SCANS = 3;

function isEligible(creationTime, startAt) {
  const created = Date.parse(creationTime);
  const start = Date.parse(startAt);
  if (!Number.isFinite(created) || !Number.isFinite(start)) {
    throw new Error("Invalid account creation time or welcome-scan launch date.");
  }
  return created >= start;
}

function initializeWallet(current, eligible, now) {
  if (current !== null && (typeof current !== "object" || Array.isArray(current))) {
    throw new Error("Invalid scan wallet.");
  }
  const wallet = { ...current };
  const remaining = wallet.remainingScans ?? 0;
  if (!Number.isSafeInteger(remaining) || remaining < 0) {
    throw new Error("Invalid scan balance.");
  }
  const grant = wallet.welcomeGrant;
  if (grant !== undefined && (
    grant === null || typeof grant !== "object" || Array.isArray(grant) ||
    grant.version !== 1 || grant.scansGranted !== WELCOME_SCANS ||
    !Number.isSafeInteger(grant.grantedAt) || grant.grantedAt < 0
  )) {
    throw new Error("Invalid welcome grant.");
  }
  if (eligible && grant === undefined) {
    if (!Number.isSafeInteger(remaining + WELCOME_SCANS)) {
      throw new Error("Scan balance is too large.");
    }
    if (!Number.isSafeInteger(now) || now < 0) {
      throw new Error("Invalid welcome grant timestamp.");
    }
    wallet.remainingScans = remaining + WELCOME_SCANS;
    wallet.welcomeGrant = { version: 1, scansGranted: WELCOME_SCANS, grantedAt: now };
  } else {
    wallet.remainingScans = remaining;
  }
  wallet.activeSubscription = wallet.remainingScans > 0;
  return wallet;
}

module.exports = { initializeWallet, isEligible };
