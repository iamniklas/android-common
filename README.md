# AndroidCommon

[![](https://jitpack.io/v/iamniklas/AndroidCommon.svg)](https://jitpack.io/#iamniklas/AndroidCommon)

AndroidCommon is a collection of some widely used implementations for various topics related to the Android library. 
The library covers topics like authentication (login and registration via Firebase with Google-SignIn and Email-SignIn), some common Firebase Services like Firestore and Remote Configuration, and many more.

The repo is far from complete, but it is only maintained for my purposes and my needs. However, feel free to add the dependency to your project if you want to use the library.
For more information about setting up the dependency, click the JitPack badge above.

## 1. How to successfully integrate android-common to your Android App

1. Create Firebase Project
2. Create Android Studio Project
3. Link App Project to Firebase Project
4. Verify the app is registered in the Firebase Project Settings
5. Set GCP Resource Location and Support email in Firebase Project Settings
6. Add Authentication SDK to Android App (Firebase Tools)
7. Enable Authentication (Google & Email) and Firestore Service
8. Add AndroidCommon Dependency and Jitpack Repository
9. Add Crashlytics Tool via Firebase Tools
10. Implement Launcher Activity
11. Add default_web_client_id with id from google-services.json
12. Add ' apply plugin: 'com.google.gms.google-services' ' to build.gradle (module-level)
13. Add SHA1 to App in Firebase

After completing all steps, user authentication via email and google should work for your app.
