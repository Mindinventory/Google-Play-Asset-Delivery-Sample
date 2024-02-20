DEBUG_BUNDLE_PATH="app/build/outputs/bundle/debug/app-debug.aab"
OUTPUT_FILE="output.apks"

export JAVA_HOME="/home/mind/.local/share/JetBrains/Toolbox/apps/android-studio/jbr"


# bundletool.jar file path
BUNDLE_TOOL="/home/mind/Downloads/bundletool.jar"

# Delete output file if exists
rm $OUTPUT_FILE

# Generate bundle
./gradlew :app:bundleDebug

# Build APKs from android bundle (.aab)
java -jar $BUNDLE_TOOL build-apks --bundle=$DEBUG_BUNDLE_PATH --output=$OUTPUT_FILE --local-testing

# Get minimum and maximum size of an APK
java -jar $BUNDLE_TOOL get-size total --apks=$OUTPUT_FILE

# Install APKs to the connected device
java -jar $BUNDLE_TOOL install-apks --apks=$OUTPUT_FILE