# Release

## General

1. Update version code in `app/build.gradle.kts` and in `iosApp/UniThen.xcodeproj/project.pbxproj`
2. Create fastlane changelog
3. Update fdroid.txt with version information (then fdroid will build and deploy it automatically)
4. `git tag v1.2.3 -a` (provide changelog)
5. `git push v1.2.3` and create release with all artifacts

## Building (Android)

IntelliJ breaks reproducible builds, build with:

1. `./gradlew app:assembleRelease`
2. `apksigner sign --ks ~/Dokumente/androidkey.jks --alignment-preserved app-release-unsigned.apk` (`app/build/outputs/apk/release/app-release-unsigned.apk`)
3. Create package in lipstick (name: `apk`, version: `1.2.3`, file name: `app-release.apk`)

## Building (iOS)

TBA

## QA

1. Ensure all tests pass on all platforms (android device, ios simulator and jvm)
2. Upgrade from previous release
3. Clear data and check if setup works correctly
4. Check on older devices (Android 9, iOS 15.6)
5. Test ticket showing
6. Test ticket scanning (with sample qr codes under [/doc](/doc))
7. Check for correct version number in about screen
8. Distribute release candidate to tutors
9. (Test if build is reproducible on F-Droid)
10. Don't early release if not needed
