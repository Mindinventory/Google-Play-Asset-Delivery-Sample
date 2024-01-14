DEBUG_BUNDLE_PATH="app/build/outputs/bundle/debug/app-debug.aab"
OUTPUT_FILE="output.apks"

export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"

# Delete output file if exists
rm $OUTPUT_FILE

# Generate bundle
./gradlew :app:bundleDebug

# Build APKs from android bundle (.aab)
bundletool build-apks --bundle=$DEBUG_BUNDLE_PATH \--output=$OUTPUT_FILE --local-testing

# Get minimum and maximum size of an APK
bundletool get-size total --apks=$OUTPUT_FILE

# Install APKs to the connected device
bundletool install-apks --apks=$OUTPUT_FILE