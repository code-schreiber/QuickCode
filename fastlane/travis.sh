#!/bin/bash

EXPECTED_TRAVIS_REPO_SLUG="code-schreiber/seamless-aperol"
EXPECTED_TRAVIS_BRANCH="develop"

# Script should bail on first error
set -e

echo "Running travis.sh"
echo "travis.sh: TRAVIS_BRANCH: $TRAVIS_BRANCH"
echo "travis.sh: TRAVIS_REPO_SLUG: $TRAVIS_REPO_SLUG"
echo "travis.sh: TRAVIS_TAG: $TRAVIS_TAG"
echo "travis.sh: TRAVIS_PULL_REQUEST: $TRAVIS_PULL_REQUEST"

cd MyWearApplication
echo "travis.sh: Running gradle printVersion"
./gradlew printVersion
echo "travis.sh: Running gradle build"
./gradlew build
echo "travis.sh: Running gradle sonarqube"
./gradlew sonarqube
echo "travis.sh: mobile/build/outputs/apk now contains:"
ls mobile/build/outputs/apk

if [ "$TRAVIS_REPO_SLUG" != "$EXPECTED_TRAVIS_REPO_SLUG" ]; then
  echo "travis.sh: Skipping deployment: wrong repository. Expected '$EXPECTED_TRAVIS_REPO_SLUG' but was '$TRAVIS_REPO_SLUG'."
elif [ "$TRAVIS_PULL_REQUEST" != "false" ]; then
  echo "travis.sh: Skipping deployment: was pull request."
elif [ "$TRAVIS_BRANCH" != "$EXPECTED_TRAVIS_BRANCH" ]; then
  echo "travis.sh: Skipping deployment: wrong branch. Expected '$EXPECTED_TRAVIS_BRANCH' but was '$TRAVIS_BRANCH'."
else
  echo "travis.sh: Running gradle firebaseUploadProdReleaseProguardMapping"
  ./gradlew firebaseUploadProdReleaseProguardMapping
  cd ..
  echo "travis.sh: Running fastlane supply"
  fastlane supply --version
  fastlane supply run --json_key dev-console-api-private-key.json --package_name com.schreiber.code.seamless.aperol --apk MyWearApplication/mobile/build/outputs/apk/mobile-prod-release.apk --track alpha
  echo "travis.sh: Deployed to Google Play"
  exit $?
fi
echo "Exiting travis.sh skipping deployment"
