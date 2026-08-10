# Release

## General

1. Update version code in `app/build.gradle.kts` and in `iosApp/UniThen.xcodeproj/project.pbxproj`
2. Create fastlane changelog
3. Update fdroid.txt with version information (then fdroid will build and deploy it automatically)
4. `git tag v1.XX -a` (provide changelog)
5. `git push v1.XX` and create release with all artifacts

## Building (Android)

IntelliJ breaks reproducible builds, build with:

1. Regenerate baseline profile (baselineprofile branch and `./gradlew app:generateBaselineProfile`) and commit it.
2. `./gradlew app:assembleRelease`
3. `apksigner sign --ks ~/Dokumente/androidkey.jks --alignment-preserved app-release-unsigned.apk` (`app/build/outputs/apk/release/app-release-unsigned.apk`)
4. Create package in lipstick (name: `apk`, version: `1.XX`, file name: `app-release.apk`)

## Building (iOS)

1. Enter `iosApp` directory
2. Create archive: `xcodebuild archive -scheme UniThen -archivePath /tmp/UniThen.xcarchive -destination "generic/platform=iOS" -allowProvisioningUpdates -configuration Release`
3. Build ipa: `xcodebuild -exportArchive -archivePath /tmp/UniThen.xcarchive -exportPath /tmp/UniThen -exportOptionsPlist ExportOptions.plist -allowProvisioningUpdates`
4. Upload `/tmp/UniThen/UniThen.ipa` file to lipstick (name: `ipa`, version: `1.XX`, file name: `app-release-unsigned.ipa`)

For distribution: TBA

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
