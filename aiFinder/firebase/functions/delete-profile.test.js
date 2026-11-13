"use strict";

const { test } = require("node:test");
const assert = require("node:assert/strict");
const { deleteProfileData, hasRecentSignIn } = require("./delete-profile");

function fixture(failAt) {
  const events = [];
  let remaining = [];
  const files = [
    "aifinder/users/alice/photos/photo.jpg",
    "aifinder/users/alice/orphan/nested.jpg",
  ].map((name) => ({
    name,
    delete: async (options) => {
      events.push(name);
      assert.deepEqual(options, { ignoreNotFound: true });
      if (failAt === "photo") throw new Error("Storage unavailable");
    },
  }));
  const services = {
    uid: "alice",
    bucket: {
      getFiles: async (options) => {
        assert.equal(options.prefix, "aifinder/users/alice/");
        return [options.maxResults ? remaining : files];
      },
    },
    database: {
      ref: () => ({
        update: async (values) => {
          events.push("database");
          assert.deepEqual(values, {
            "aifinder/users/alice": null,
            "aifinder/appReviews/alice": null,
          });
          if (failAt === "database") throw new Error("Database unavailable");
        },
      }),
    },
    auth: {
      deleteUser: async (uid) => {
        assert.equal(uid, "alice");
        events.push("auth");
        if (failAt === "auth") throw new Error("Auth unavailable");
      },
    },
    deleteSubscriber: async (uid) => {
      assert.equal(uid, "alice");
      events.push("subscriber");
      if (failAt === "subscriber") throw new Error("Purchase service unavailable");
    },
  };
  return { services, events, addConcurrentUpload: () => { remaining = [{}]; } };
}

test("requires a sign-in within fifteen minutes; absent, stale and future dates fail", () => {
  const now = 2_000_000;
  assert.equal(hasRecentSignIn({ auth_time: 2000 }, now), true);
  assert.equal(hasRecentSignIn({ auth_time: 1100 }, now), true);
  for (const token of [{}, { auth_time: 1099 }, { auth_time: 2001 }, { auth_time: "bad" }]) {
    assert.equal(hasRecentSignIn(token, now), false);
  }
});

test("deletes subscriber, every user object, database plus feedback, then Auth", async () => {
  const { services, events } = fixture();
  await deleteProfileData(services);
  assert.deepEqual(events, [
    "subscriber",
    "aifinder/users/alice/photos/photo.jpg",
    "aifinder/users/alice/orphan/nested.jpg",
    "database",
    "auth",
  ]);
});

for (const stage of ["subscriber", "photo", "database", "auth"]) {
  test(`propagates ${stage} failure and permits retry rather than claiming success`, async () => {
    const { services, events } = fixture(stage);
    await assert.rejects(deleteProfileData(services));
    if (stage !== "auth") assert.ok(!events.includes("auth"));
  });
}

test("unexpected cross-account object fails closed", async () => {
  const { services, events } = fixture();
  services.bucket.getFiles = async () => [[{
    name: "aifinder/users/bob/photos/photo.jpg",
    delete: async () => assert.fail("Must not delete another user's photos"),
  }]];
  await assert.rejects(deleteProfileData(services), /Unexpected storage object/);
  assert.deepEqual(events, ["subscriber"]);
});

test("a concurrent upload prevents reporting complete deletion", async () => {
  const { services, events, addConcurrentUpload } = fixture();
  addConcurrentUpload();
  await assert.rejects(deleteProfileData(services), /still being uploaded/);
  assert.ok(!events.includes("database"));
  assert.ok(!events.includes("auth"));
});

test("retries tolerate an already absent Auth user and empty storage", async () => {
  const { services } = fixture();
  services.bucket.getFiles = async () => [[]];
  services.auth.deleteUser = async () => { throw Object.assign(new Error("Absent"), { code: "auth/user-not-found" }); };
  await deleteProfileData(services);
});

test("invalid user paths are rejected before any external operation", async () => {
  for (const uid of ["", "../bob", "alice/bob", undefined]) {
    const { services, events } = fixture();
    await assert.rejects(deleteProfileData({ ...services, uid }), /Invalid account/);
    assert.deepEqual(events, []);
  }
});
