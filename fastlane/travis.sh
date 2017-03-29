#!/bin/bash

# Script should bail on first error
set -e

echo "Running travis.sh"
echo "travis.sh: TRAVIS_BRANCH: $TRAVIS_BRANCH"
echo "travis.sh: TRAVIS_REPO_SLUG: $TRAVIS_REPO_SLUG"
echo "travis.sh: TRAVIS_TAG: $TRAVIS_TAG"
echo "travis.sh: TRAVIS_PULL_REQUEST: $TRAVIS_PULL_REQUEST"

cd MyWearApplication
./gradlew printVersion
echo "travis.sh: Running gradle build"
./gradlew build
echo "travis.sh: Running gradle sonarqube"
./gradlew sonarqube
echo "travis.sh: mobile/build/outputs/apk now contains:"
ls mobile/build/outputs/apk
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
  fastlane supply run -json_key private_key.json -package_name com.schreiber.code.seamless.aperol --apk MyWearApplication/mobile/build/outputs/apk/mobile-prod-release.apk --track alpha
  echo "travis.sh: Deployed to Google Play"
  exit $?
fi
echo "Exiting travis.sh"
