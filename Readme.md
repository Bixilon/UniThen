# UniThen

This android application is for doing the QR code check in (and a bit more) for [UniNow](https://uninow.com/) courses.
It can be used for any booking system hosted by them, e.g. for the [ZHS München](https://kurse.zhs-muenchen.de).

This app is **NOT** affiliated with UniNow GmbH, the provider/developer of the booking system.
If something with *this* app does not work, please contact [me](https://bixilon.de) and NOT UniNow. They can't and won't help you.

## Features

- Multiple sites and accounts
- QR Code Check in
- List courses/appointments
- Really fast, no ads, no trackers
- Simple and small
- Completely offline (authenticate once and refresh courses manually at any time)

## Download

[<img src="doc/fdroid.svg" alt="Get it on F-Droid" height="80">](https://f-droid.org/packages/de.bixilon.unithen)

The latest release is published on [gitlab releases](https://gitlab.bixilon.de/bixilon/unithen/-/releases). and on [F-Droid](https://f-droid.org/packages/de.bixilon.unithen). F-Droid builds are reproducible,
and signed with my key (SHA512: `f44dcdebfb54333fa205ff11eaa5aa1f47cde8217dd63a9fd979cd1fcf6d4241`) too. F-Droid is the preferred way, then you don't need to worry about updates.

(This app is Android only, iOS is **NOT** supported and won't be)

## Why

So, the original [UniNow app](https://play.google.com/store/apps/details?id=de.mocama.UniNow) is not that bad (tries to be privacy friendly, works offline), but there are a few points that really bother me:

Doing simple things needs a lot of user interaction (e.g. when I want to do the check in):
Open app -> (wait) -> No I am not interested in improving the app -> (Must look at ads) -> My Studies -> ZHS -> (wait) -> Find the course -> (wait) -> Scroll down -> QR Code -> (wait)

And I don't want anything on my phone that I don't essentially need and that is not open source*.

## Under the hood

(Everything as simple as possible)

- Webview for loading UniNow website + sniff (and store) cookie
- Fetch user and page details and extract them from html (this could be improved with a dedicated api endpoint, did not touch the app yet)
- Get courses and appointments with GraphQL
- Store everything locally in SQL database

## Releasing (Note for myself)

IntelliJ breaks reproducible builds, build with:

1. Create fastlane changelog
2. `git tag v1.2.3`
3. `./gradlew app:assembleRelease`
4. `apksigner sign --ks ~/Dokumente/androidkey.jks --alignment-preserved app-release-unsigned.apk`
5. `curl --location --header "PRIVATE-TOKEN: XXXXXX" --upload-file app-release-signed.apk" "https://gitlab.bixilon.de/api/v4/projects/444/packages/generic/apk/VERSION/app-release.apk"`
6. Push tags & create release (fdroid builds automatically)
