#!/bin/bash

echo "Running travis.sh"
echo "travis.sh: TRAVIS_BRANCH: $TRAVIS_BRANCH"
echo "travis.sh: TRAVIS_REPO_SLUG: $TRAVIS_REPO_SLUG"
echo "travis.sh: TRAVIS_TAG: $TRAVIS_TAG"
echo "travis.sh: TRAVIS_PULL_REQUEST: $TRAVIS_PULL_REQUEST"

ls
cd MyWearApplication
ls
echo "travis.sh: Running gradle build"
./gradlew build
echo "travis.sh: mobile/build/outputs/apk now contains:"
ls mobile/build/outputs/apk
# echo "travis.sh: Running gradle sonarqube"
# ./gradlew sonarqube
cd ..
ls

if [ "$TRAVIS_PULL_REQUEST" != "false" ]; then
  echo "travis.sh: Skipping snapshot deployment: was pull request."
elif [ "$TRAVIS_BRANCH" == "develop" ]; then
  echo "travis.sh: Running fastlane supply --version"
  fastlane supply --version
  echo "travis.sh: Running fastlane supply run"
  fastlane supply run -j "private_key.json" -p com.schreiber.code.seamless.aperol -b MyWearApplication/mobile/build/outputs/apk/mobile-release.apk -a alpha --validate_only true
  echo "travis.sh: Exiting fastlane supply"
  exit $?
fi
echo "Exiting travis.sh"
