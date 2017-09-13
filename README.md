# Q-Mail
[![Build Status](https://philipwhiuk.ci.cloudbees.com/job/master/badge/icon)](https://philipwhiuk.ci.cloudbees.com/job/master/)

Q-Mail is an open-source email client for Android.

## Download

Q-Mail can be downloaded from a couple of sources:

- [Google Play](https://play.google.com/store/apps/details?id=com.whiuk.philip.qmail)
- [Github Releases](https://github.com/philipwhiuk/q-mail/releases)

You might also be interested in becoming a 
[tester](https://play.google.com/apps/testing/com.whiuk.philip.qmail) 
to get an early look at new versions.


## Release Notes

Check out the [Release Notes](https://github.com/k9mail/k-9/wiki/ReleaseNotes) to find out what changed
in each version of Q-Mail.


## Need Help?

If the app is not behaving like it should, you might find these resources helpful:

- [User Manual](https://philipwhiuk.github.io/qmail/documentation.html)
- [Frequently Asked Questions](https://philipwhiuk.github.io/qmail/documentation/faq.html)

## Translations

Interested in helping to translate K-9 Mail? Contribute here:

https://www.transifex.com/projects/p/qmail/

## Roadmap

Features required for 1.0.0 Release

* Material Design
* Target SDK: API 26
* Microsoft Exchange Support
* Google Mail labels support
* Autocrypt Support
* Full OpenPGP and PGP/MIME support
* S/MIME support
* ICS support (read and reply)
* Removal of 'errors' folder
* Log file generation
* Easy in-app bug reporting (stacktrace to GitHub).

## Versioning

Q-Mail uses semantic versioning (major, minor, patch).

## Design

A re-design is underway. The aim is to provide a Material interface suitable for modern smartphones.

## Contributing

Please fork this repository and contribute back using [pull requests](https://github.com/philipwhiuk/q-mail/pulls).

Any contributions, large or small, major features, bug fixes, unit/integration tests are welcomed and appreciated
but will be thoroughly reviewed and discussed. Please make sure you read the [Code Style Guidelines](https://github.com/philipwhiuk/q-mail/CODESTYLE.md).

## Ecosystem

As a fork of the K-9 project, we try to upstream features to them. We also look at other forks. 
When working out what features to adopt our focus is on deployed status and usage as well as 
forward-thinking, security and reliability.

We also try and keep migration between K-9 and Q-Mail as easy as possible.


## Communication

All communication is done via GitHub - issues will be flaired appropriately.


## License

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

## Dependencies

* ews-android-api (MIT License)
* ews-java-api (MIT License)
* biweekly (BSD 2-clause License)
* openpgp-api-lib (Apache Version 2.0)
* smime-api-lib (Apache Version 2.0)
