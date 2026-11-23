const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ADMIN_UID = "admin_gopal";
const ADMIN_EMAIL = "dasgopal292@gmail.com";
const CUSTOMER_UID = "customer_rahul";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function adminDb() {
  return testEnv
    .authenticatedContext(ADMIN_UID, {
      email: ADMIN_EMAIL,
      email_verified: true,
    })
    .firestore();
}

function unverifiedAdminSpoofDb() {
  return testEnv
    .authenticatedContext("spoof_uid", {
      email: ADMIN_EMAIL,
      email_verified: false,
    })
    .firestore();
}

function sampleServicePayload(now = new Date(Date.now() - 5000)) {
  return {
    name: "8x10 Photo Frame",
    hindiName: "8x10 फोटो फ्रेम (गोल्ड वुड)",
    category: "PHOTO_FRAMES",
    priceInr: 450,
    unitLabel: "Per Frame",
    description: "Premium brushed gold and matte black wooden frame with anti-glare glass.",
    presetKey: "frames",
    available: true,
    featured: true,
    sortOrder: 3,
    createdAt: now,
    updatedAt: now,
  };
}

function sampleOrderPayload(now = new Date(Date.now() - 5000)) {
  return {
    customerName: "Aarav Sharma",
    customerPhone: "+91 9876543210",
    requirements: "Need glossy finish, urgent pickup tomorrow 5 PM",
    itemsSummary: "2x 8x10 Photo Frame (₹900), 1x Urgent Passport Photo (₹200)",
    itemCount: 3,
    totalInr: 1100,
    status: "PENDING",
    createdAt: now,
    updatedAt: now,
  };
}

test("Customers without login can browse services, studio config, offers, and reviews", async () => {
  const now = new Date(Date.now() - 5000);
  await assertSucceeds(adminDb().collection("services").doc("frame_8x10").set(sampleServicePayload(now)));
  await assertSucceeds(
    adminDb().collection("studio_config").doc("main").set({
      businessName: "Studio Pixy",
      tagline: "Timeless Frames, Portraits & Cinematic Memories",
      whatsappNumber: "+919876543210",
      phoneNumber: "+919876543210",
      address: "Shop 12, Main Market Road, Near City Center",
      email: "hello@studiopixy.in",
      openingHours: "Mon - Sun: 9:30 AM - 9:00 PM",
      mapsQuery: "Studio Pixy Photography Studio",
      specialOfferTitle: "Festive Frame Combo",
      specialOfferDetails: "Flat 15% OFF on 12x18 & 16x20 Wedding Frames",
      updatedAt: now,
    })
  );

  const guestDb = testEnv.unauthenticatedContext().firestore();
  await assertSucceeds(guestDb.collection("services").get());
  await assertSucceeds(guestDb.collection("studio_config").doc("main").get());
  await assertSucceeds(guestDb.collection("offers").get());
  await assertSucceeds(guestDb.collection("reviews").get());
});

test("Non-admins and spoofed unverified email cannot modify services or studio config", async () => {
  const now = new Date(Date.now() - 5000);
  const guestDb = testEnv.unauthenticatedContext().firestore();
  const userDb = testEnv.authenticatedContext(CUSTOMER_UID, { email: "user@example.com", email_verified: true }).firestore();

  await assertFails(guestDb.collection("services").doc("frame_8x10").set(sampleServicePayload(now)));
  await assertFails(userDb.collection("services").doc("frame_8x10").set(sampleServicePayload(now)));
  await assertFails(unverifiedAdminSpoofDb().collection("services").doc("frame_8x10").set(sampleServicePayload(now)));
});

test("Admin can create, partially update price, and delete services; rejects @DocumentId id and ghost keys", async () => {
  const created = new Date(Date.now() - 10000);
  const serviceRef = adminDb().collection("services").doc("frame_8x10");

  await assertSucceeds(serviceRef.set(sampleServicePayload(created)));
  await assertSucceeds(
    serviceRef.update({
      priceInr: 499,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject immutable createdAt change
  await assertFails(
    serviceRef.update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject @DocumentId "id" key inside payload
  const withId = { ...sampleServicePayload(created), id: "frame_bad" };
  await assertFails(adminDb().collection("services").doc("frame_bad").set(withId));

  // Reject shadow/ghost field
  const withGhost = { ...sampleServicePayload(created), isAdmin: true };
  await assertFails(adminDb().collection("services").doc("frame_ghost").set(withGhost));

  // Reject future timestamp
  await assertFails(
    adminDb().collection("services").doc("frame_future").set(sampleServicePayload(new Date(Date.now() + 60000)))
  );

  await assertSucceeds(serviceRef.delete());
});

test("Orders PII protection: customers can create PENDING orders without login, cannot read/list/update/delete; only Admin can manage", async () => {
  const guestDb = testEnv.unauthenticatedContext().firestore();
  const now = new Date(Date.now() - 5000);

  // Customer creates PENDING order
  await assertSucceeds(guestDb.collection("orders").doc("ord_1001").set(sampleOrderPayload(now)));

  // Customer cannot shortcut status to COMPLETED on create
  await assertFails(
    guestDb.collection("orders").doc("ord_1002").set({
      ...sampleOrderPayload(now),
      status: "COMPLETED",
    })
  );

  // Customer or signed-in non-admin cannot read or list orders (PII protection)
  await assertFails(guestDb.collection("orders").doc("ord_1001").get());
  await assertFails(guestDb.collection("orders").get());

  const nonAdminDb = testEnv.authenticatedContext(CUSTOMER_UID, { email: "other@example.com", email_verified: true }).firestore();
  await assertFails(nonAdminDb.collection("orders").get());
  await assertFails(nonAdminDb.collection("orders").doc("ord_1001").update({ status: "CONFIRMED", updatedAt: new Date(Date.now() - 1000) }));

  // Admin can list and update status
  await assertSucceeds(adminDb().collection("orders").get());
  await assertSucceeds(
    adminDb().collection("orders").doc("ord_1001").update({
      status: "CONFIRMED",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertSucceeds(
    adminDb().collection("orders").doc("ord_1001").update({
      status: "COMPLETED",
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Offers protection: unauthorized users cannot create, update, or delete offers; Admin can manage offers", async () => {
  const now = new Date(Date.now() - 5000);
  const offerPayload = {
    title: "Diwali Frame Offer",
    badge: "20% OFF",
    description: "Discount on 12x18 photo frames.",
    promoCode: "DIWALI20",
    active: true,
    createdAt: now,
    updatedAt: now,
  };

  const guestDb = testEnv.unauthenticatedContext().firestore();
  const nonAdminDb = testEnv
    .authenticatedContext(CUSTOMER_UID, { email: "user@example.com", email_verified: true })
    .firestore();

  await assertFails(guestDb.collection("offers").doc("offer_1").set(offerPayload));
  await assertFails(nonAdminDb.collection("offers").doc("offer_1").set(offerPayload));
  await assertFails(unverifiedAdminSpoofDb().collection("offers").doc("offer_1").set(offerPayload));

  await assertSucceeds(adminDb().collection("offers").doc("offer_1").set(offerPayload));
  await assertSucceeds(
    adminDb().collection("offers").doc("offer_1").update({
      active: false,
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(guestDb.collection("offers").doc("offer_1").delete());
  await assertFails(nonAdminDb.collection("offers").doc("offer_1").delete());
  await assertSucceeds(adminDb().collection("offers").doc("offer_1").delete());
});

test("Service photos: only Admin can update or delete imageUrl; customers and unverified accounts are denied", async () => {
  const created = new Date(Date.now() - 10000);
  const serviceRef = adminDb().collection("services").doc("frame_8x10");
  await assertSucceeds(serviceRef.set(sampleServicePayload(created)));

  const storageUrl =
    "https://firebasestorage.googleapis.com/v0/b/gen-lang-client-0184859803.firebasestorage.app/o/service_photos%2Fframe_8x10%2Fphoto_1700000000000.jpg?alt=media&token=abc123";

  // Guest & signed-in non-admin customers must be blocked from modifying service photos
  const guestDb = testEnv.unauthenticatedContext().firestore();
  const nonAdminDb = testEnv
    .authenticatedContext(CUSTOMER_UID, { email: "customer@example.com", email_verified: true })
    .firestore();

  await assertFails(
    guestDb.collection("services").doc("frame_8x10").update({
      imageUrl: storageUrl,
      updatedAt: new Date(Date.now() - 2000),
    })
  );
  await assertFails(
    nonAdminDb.collection("services").doc("frame_8x10").update({
      imageUrl: storageUrl,
      updatedAt: new Date(Date.now() - 2000),
    })
  );
  await assertFails(
    unverifiedAdminSpoofDb().collection("services").doc("frame_8x10").update({
      imageUrl: storageUrl,
      updatedAt: new Date(Date.now() - 2000),
    })
  );

  // Admin can save & replace Storage download URL on service
  await assertSucceeds(
    serviceRef.update({
      imageUrl: storageUrl,
      updatedAt: new Date(Date.now() - 1500),
    })
  );

  // Reject oversized imageUrl (> 600 chars)
  await assertFails(
    serviceRef.update({
      imageUrl: "https://firebasestorage.googleapis.com/" + "a".repeat(600),
      updatedAt: new Date(Date.now() - 1200),
    })
  );

  // Admin can delete custom photo by setting imageUrl to null
  await assertSucceeds(
    serviceRef.update({
      imageUrl: null,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Customers can still read the service record without login
  await assertSucceeds(guestDb.collection("services").doc("frame_8x10").get());
});
