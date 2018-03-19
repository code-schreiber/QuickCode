Master

[![Build Status](https://travis-ci.org/code-schreiber/QuickCode.svg?branch=master)](https://travis-ci.org/code-schreiber/QuickCode) [![Quality Gate](https://sonarcloud.io/api/badges/gate?key=QuickCode)](https://sonarcloud.io/dashboard?id=QuickCode)


Develop

[![Build Status](https://travis-ci.org/code-schreiber/QuickCode.svg?branch=develop)](https://travis-ci.org/code-schreiber/QuickCode) [![Quality Gate](https://sonarcloud.io/api/badges/gate?key=QuickCode%3Adevelop)](https://sonarcloud.io/dashboard?id=QuickCode%3Adevelop)

<p align="center">
 <b>Quick Code</b>
 <br>
 <img src='https://github.com/code-schreiber/QuickCode/raw/develop/fastlane/metadata/android/en-US/images/icon.png' width='100' height='100'/>
 <br>
 <a href='https://play.google.com/store/apps/details?id=com.toolslab.quickcode&utm_source=github'>
  <img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' width='200'/>
 </a>
</p>

Where do you usually put a code that will be scanned soon? 🤔

With Quick Code, you can instantly be ready for showing your code to be scanned in seconds, even when offline!
Just import your pdf or image to Quick Code and have it two taps away. You can then instantly show your e-ticket anywhere you are, at an event, at the station or even at the airport ✈️

Features to come:
- App shortcuts, reducing the time to get to your code to one single tap from the home screen
- Android Wear integration
- Just-in-time notifications to get to your code even quicker from the notification bar

Currently supported code formats are:
- QR Code
- PDF417
- Aztec

This app is
[![Open Source Love svg3](https://badges.frapsoft.com/os/v3/open-source.svg?v=103)](https://github.com/ellerbrock/open-source-badges/)
and is available on Google Play: https://play.google.com/store/apps/developer?id=Tools+Lab

Become an early access tester at https://play.google.com/apps/testing/com.toolslab.quickcode

Share your thoughts and feedback at https://quickcode.uservoice.com

###### Technical details and used technologies
This project uses
1. Android Vision for image recognition
1. Firebase for crash reporting and offline database.
1. AutoValue to create immutable objects
1. Data Binding to connect XML views with the data to be shown.

The app is built, tested and deployed to Google Play automatically with the help of Travis and Fastlane Supply.  
Code quality metrics are measured by SonarCloud.
