#!/bin/sh

echo "Running travis.sh"
echo "TRAVIS_TAG: $TRAVIS_TAG"
echo "TRAVIS_PULL_REQUEST: $TRAVIS_PULL_REQUEST"

cd MyWearApplication
echo "Running gradle build"
./gradlew build
# echo "Running gradle sonarqube"
# ./gradlew sonarqube
cd ..

if [[ "$TRAVIS_PULL_REQUEST" != "false" ]]; then
  echo "Running fastlane supply -v"
  fastlane supply -v
  echo "Running fastlane supply run"
  fastlane supply run -j "private_key.json" -p com.schreiber.code.seamless.aperol -b MyWearApplication/mobile/build/outputs/apk/mobile-release.apk -a alpha --validate_only true
  echo "Exiting fastlane supply"
  exit $?
echo "Exiting travis.sh"
fi