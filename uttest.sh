./gradlew.bat :dev-sdk:assembleAndroidTest
./gradlew.bat :app:assembleRelease
java -jar tools/spoon/spoon.jar --test-apk ./dev-sdk/build/outputs/apk/androidTest/debug/dev-sdk-debug-androidTest.apk --apk ./app/build/outputs/apk/release/app-release.apk --output ./spoon/
