"use strict";

// Keep Auth until all data removal succeeds so a partial failure can be retried.
async function deleteProfileData({ uid, bucket, database, auth, deleteSubscriber }) {
  if (typeof uid !== "string" || !uid || /[.#$[\]/]/.test(uid)) {
    throw new Error("Invalid account identifier.");
  }
  await deleteSubscriber(uid);
  const prefix = `aifinder/users/${uid}/`;
  // Listing the whole prefix also finds orphaned photos and nested objects.
  const [files] = await bucket.getFiles({ prefix, versions: true });
  for (const file of files) {
    if (!file.name.startsWith(prefix)) throw new Error("Unexpected storage object.");
    await file.delete({ ignoreNotFound: true });
  }
  const [remaining] = await bucket.getFiles({ prefix, maxResults: 1, autoPaginate: false });
  if (remaining.length) throw new Error("Account photos are still being uploaded. Retry deletion.");
  await database.ref().update({
    [`aifinder/users/${uid}`]: null,
    [`aifinder/appReviews/${uid}`]: null,
  });
  try {
    await auth.deleteUser(uid);
  } catch (error) {
    if (error.code !== "auth/user-not-found") throw error;
  }
}

function hasRecentSignIn(token, now = Date.now()) {
  if (typeof token?.auth_time !== "number") return false;
  const age = Math.floor(now / 1000) - token?.auth_time;
  return Number.isFinite(age) && age >= 0 && age <= 15 * 60;
}

module.exports = { deleteProfileData, hasRecentSignIn };
