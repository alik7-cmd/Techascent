"use strict";

const { initializeApp } = require("firebase-admin/app");
const { getAuth } = require("firebase-admin/auth");
const { getDatabaseWithUrl } = require("firebase-admin/database");
const { defineString, defineSecret } = require("firebase-functions/params");
const { getStorage } = require("firebase-admin/storage");
const { onCall, HttpsError } = require("firebase-functions/v2/https");
const logger = require("firebase-functions/logger");
const { initializeWallet, isEligible } = require("./welcome");
const { deleteProfileData, hasRecentSignIn } = require("./delete-profile");

const app = initializeApp();
const databaseUrl = defineString("AIFINDER_DATABASE_URL");
const startAt = defineString("WELCOME_SCANS_START_AT");
const storageBucket = defineString("AIFINDER_STORAGE_BUCKET");
const revenueCatSecret = defineSecret("REVENUECAT_SECRET_API_KEY");

exports.deleteProfile = onCall({
  region: "us-central1",
  timeoutSeconds: 540,
  secrets: [revenueCatSecret],
}, async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Sign in before deleting your account.");
  }
  if (!hasRecentSignIn(request.auth.token)) {
    throw new HttpsError("failed-precondition",
      "For your security, sign out, sign in again, then retry deleting your account.");
  }
  try {
    const bucket = storageBucket.value().replace(/^gs:\/\//, "").replace(/\/$/, "");
    if (!bucket || bucket.includes("/")) throw new Error("Invalid storage bucket configuration.");
    await deleteProfileData({
      uid: request.auth.uid,
      bucket: getStorage(app).bucket(bucket),
      database: getDatabaseWithUrl(databaseUrl.value(), app),
      auth: getAuth(app),
      deleteSubscriber: async (uid) => {
        const response = await fetch(`https://api.revenuecat.com/v1/subscribers/${encodeURIComponent(uid)}`, {
          method: "DELETE",
          headers: { Authorization: `Bearer ${revenueCatSecret.value()}` },
          signal: AbortSignal.timeout(30_000),
        });
        if (!response.ok && response.status !== 404) {
          throw new Error(`Purchase profile deletion failed (${response.status}).`);
        }
      },
    });
    return { deleted: true };
  } catch (error) {
    logger.error("Account deletion failed.", { uid: request.auth.uid, error });
    throw new HttpsError("internal",
      "Deletion could not finish. Some data may already be removed. Retry, or contact bdtechascent@gmail.com.");
  }
});

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
