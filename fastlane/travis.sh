#!/bin/bash

# Script should bail on first error
set -e

echo "Running travis.sh"
echo "travis.sh: TRAVIS_BRANCH: $TRAVIS_BRANCH"
echo "travis.sh: TRAVIS_REPO_SLUG: $TRAVIS_REPO_SLUG"
echo "travis.sh: TRAVIS_TAG: $TRAVIS_TAG"
echo "travis.sh: TRAVIS_PULL_REQUEST: $TRAVIS_PULL_REQUEST"

cd MyWearApplication
echo "travis.sh: Running gradle build"
./gradlew build
echo "travis.sh: mobile/build/outputs/apk now contains:"
ls mobile/build/outputs/apk
# echo "travis.sh: Running gradle sonarqube"
# ./gradlew sonarqube
cd ..

if [ "$TRAVIS_REPO_SLUG" != "code-schreiber/seamless-aperol" ]; then
  echo "travis.sh: Skipping deployment: wrong repository. Expected code-schreiber/seamless-aperol but was '$TRAVIS_REPO_SLUG'."
elif [ "$TRAVIS_PULL_REQUEST" != "false" ]; then
  echo "travis.sh: Skipping deployment: was pull request."
elif [ "$TRAVIS_BRANCH" != "develop" ]; then
  echo "travis.sh: Skipping deployment: wrong branch. Expected develop but was '$TRAVIS_BRANCH'."
else
  echo "travis.sh: Running fastlane supply"
  fastlane supply --version
  fastlane supply run -j "private_key.json" -p com.schreiber.code.seamless.aperol -b MyWearApplication/mobile/build/outputs/apk/mobile-release.apk -a alpha
  echo "travis.sh: Deployed to Google Play"
  exit $?
fi
echo "Exiting travis.sh"
