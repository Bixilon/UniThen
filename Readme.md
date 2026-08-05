# UniThen

![Logo](logo.svg)

![](https://shields-io.bixilon.de/f-droid/v/de.bixilon.unithen?style=for-the-badge&logo=fdroid)
![](https://shields-io.bixilon.de/gitlab/v/release/bixilon/unithen?branch=master&gitlab_url=https%3A%2F%2Fgitlab.bixilon.de&style=for-the-badge)

This app is for performing the QR code check in (and a bit more) for [UniNow](https://uninow.com/) courses.
It can be used for any digital booking system (DBS) hosted by them, e.g. for the [ZHS München](https://kurse.zhs-muenchen.de).

This app is **NOT** affiliated with UniNow GmbH, the provider/developer of the booking system.
If something with *this* app does not work, please contact [me](https://bixilon.de) and NOT UniNow. They can't and won't help you.

## Features

- Multiple sites and accounts
- QR Code check in (presenting and scanning)
- List of your courses
- Really fast, no ads, no trackers
- Simple and small (~4MB; most of it is due to qr code scanning from [zxing-cpp](https://github.com/zxing-cpp/zxing-cpp))
- Completely offline (authenticate once)

## Download

### Android
[<img src="doc/fdroid.svg" alt="Get it on F-Droid" height="80">](https://f-droid.org/packages/de.bixilon.unithen)

The latest release is published on [gitlab releases](https://gitlab.bixilon.de/bixilon/unithen/-/releases). and on [F-Droid](https://f-droid.org/packages/de.bixilon.unithen). F-Droid builds are reproducible,
and signed with my key (SHA512: `f44dcdebfb54333fa205ff11eaa5aa1f47cde8217dd63a9fd979cd1fcf6d4241`) too. F-Droid is the preferred way, then you don't need to worry about updates.
Android 8+ is offically supported.

### iOS
iOS is fully supported. There is no distribution yet, so you must build the app yourself (requires iOS 15.6+).

### Desktop
This app is ported to the jvm with compose multiplatform and runs on desktop, however this is a show off and not an offical supported platform. Please build it yourself.

## Screenshots

| **Course overview** ![Course overview](fastlane/metadata/android/en-US/images/phoneScreenshots/1.png) | **QR Code Check In** ![QR Code](fastlane/metadata/android/en-US/images/phoneScreenshots/2.png)  |
|:-----------------------------------------------------------------------------------------------------:|:-----------------------------------------------------------------------------------------------:|
|      **Attendee List** ![QR Code](fastlane/metadata/android/en-US/images/phoneScreenshots/3.png)      | **QR Code Scanning** ![Scanning](fastlane/metadata/android/en-US/images/phoneScreenshots/4.png) |

## Why

So, the original [UniNow app](https://play.google.com/store/apps/details?id=de.mocama.UniNow) is not that bad (tries to be privacy friendly), but there are a few points that really bother me:

Doing simple things needs a lot of user interaction (e.g. when I want to show my ticket for the check in):
Open app -> (wait) -> No I am not interested in improving the app -> (Must look at ads) -> My Studies -> ZHS -> (wait) -> Find the course -> (wait) -> Scroll down -> QR Code -> (wait)

And I don't want it on my phone :)

If somebody from UniNow sees this, please reach out **before** blocking the app off. I do not want to harm you (with this app).

## Under the hood

(Everything as simple as possible)

- Ory authentication and webview as fallback for acquiring an access token
- Get page details (extract from html), and get user user details
- Get courses and appointments with GraphQL ([Schema](./doc/UniNow.graphql))
- Store everything on your device in a SQL database
- QR code scanning: Local copy of all enrolled users, queue for offline synchronization and [fts4](https://www.sqlite.org/fts3.html) for searching (actually kinda complex)

## Something is broken
Please report an [issue](https://gitlab.bixilon.de/bixilon/unithen/-/issues) (you must register for an account first), or send me a quick email to `bixilon [a.t] bixilon [dot.] de`. I also appreciate feedback (positive or negative) :smile:

Every use case is different, mine is just checking in for sports courses and that works pretty much offline.

## Code

All code is open source and licensed under the terms of the General Public Licence v3 or later. The source code can be retrieved on [gitlab.bixilon.de](https://gitlab.bixilon.de/bixilon/unithen). There is a [GitHub mirror](https://github.com/Bixilon/UniThen). Its only for code preservation, please don't report issues nor publish pull requests there.
