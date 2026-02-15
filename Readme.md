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
The latest release is publish on [gitlab releases](https://gitlab.bixilon.de/bixilon/unithen/-/releases). F-Droid coming soon.

The releases are signed with my release key (SHA512: `f44dcdebfb54333fa205ff11eaa5aa1f47cde8217dd63a9fd979cd1fcf6d4241`).

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
