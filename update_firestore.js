const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore, FieldValue } = require('firebase-admin/firestore');
const serviceAccount = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT);


initializeApp({
credential: cert(serviceAccount)
});

const db = getFirestore();

async function updateRelease() {
await db.collection('configTest').doc('config').set({
  latestVersionCode: parseInt(process.env.VERSION_CODE),
  updatedOn: FieldValue.serverTimestamp()
}, { merge: true });

console.log('Firestore document updated successfully');
}

updateRelease().catch(console.error);
