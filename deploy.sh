#!/bin/bash

EXPECTED_TRAVIS_REPO_SLUG="code-schreiber/QuickCode"
EXPECTED_TRAVIS_BRANCH="develop"

# Script should bail on first error
set -e

echo "Running deploy.sh"
echo "deploy.sh: TRAVIS_BRANCH: $TRAVIS_BRANCH"
echo "deploy.sh: TRAVIS_REPO_SLUG: $TRAVIS_REPO_SLUG"
echo "deploy.sh: TRAVIS_TAG: $TRAVIS_TAG"
echo "deploy.sh: TRAVIS_PULL_REQUEST: $TRAVIS_PULL_REQUEST"

cd QuickCode
echo "deploy.sh: Running gradle printVersion"
./gradlew printVersion
echo "deploy.sh: Running gradle build"
./gradlew build
echo "deploy.sh: Running gradle sonarqube"
./gradlew sonarqube
echo "deploy.sh: mobile/build/outputs/apk/release now contains:"
ls mobile/build/outputs/apk/release

if [ "$TRAVIS_REPO_SLUG" != "$EXPECTED_TRAVIS_REPO_SLUG" ]; then
  echo "deploy.sh: Skipping deployment: wrong repository. Expected '$EXPECTED_TRAVIS_REPO_SLUG' but was '$TRAVIS_REPO_SLUG'."
elif [ "$TRAVIS_PULL_REQUEST" != "false" ]; then
  echo "deploy.sh: Skipping deployment: was pull request."
elif [ "$TRAVIS_BRANCH" != "$EXPECTED_TRAVIS_BRANCH" ]; then
  echo "deploy.sh: Skipping deployment: wrong branch. Expected '$EXPECTED_TRAVIS_BRANCH' but was '$TRAVIS_BRANCH'."
else
  echo "deploy.sh: Running gradle firebaseUploadReleaseProguardMapping"
  ./gradlew firebaseUploadReleaseProguardMapping
  cd ..
  echo "deploy.sh: Running fastlane supply"
  fastlane supply --version
  fastlane supply run --json_key dev-console-api-private-key.json --package_name com.toolslab.quickcode --apk QuickCode/mobile/build/outputs/apk/release/mobile-release.apk --track alpha
  echo "deploy.sh: Deployed to Google Play"
  exit $?
fi
echo "Exiting deploy.sh, skipping deployment"
