"use strict";

const { initializeApp } = require("firebase-admin/app");
const { getAuth } = require("firebase-admin/auth");
const { getDatabaseWithUrl } = require("firebase-admin/database");
const { defineString } = require("firebase-functions/params");
const { onCall, HttpsError } = require("firebase-functions/v2/https");
const logger = require("firebase-functions/logger");
const { initializeWallet, isEligible } = require("./welcome");

const app = initializeApp();
const databaseUrl = defineString("AIFINDER_DATABASE_URL");
const startAt = defineString("WELCOME_SCANS_START_AT");

exports.ensureScanWallet = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Sign in to check scan credits.");
  }
  try {
    const user = await getAuth(app).getUser(request.auth.uid);
    if (user.disabled) {
      throw new HttpsError("permission-denied", "This account is disabled.");
    }
    const eligible = user.providerData.length > 0 &&
      isEligible(user.metadata.creationTime, startAt.value());
    const ref = getDatabaseWithUrl(databaseUrl.value(), app)
      .ref(`aifinder/users/${user.uid}/scanWallet`);
    const now = Date.now();
    const result = await ref.transaction((current) => initializeWallet(current, eligible, now));
    if (!result.committed) {
      throw new Error("Scan wallet initialization was not committed.");
    }
    return { remainingScans: result.snapshot.child("remainingScans").val() };
  } catch (error) {
    if (error instanceof HttpsError) throw error;
    logger.error("Scan wallet initialization failed.", { uid: request.auth.uid, error });
    throw new HttpsError("internal", "Could not check scan credits. Please try again.");
  }
});
