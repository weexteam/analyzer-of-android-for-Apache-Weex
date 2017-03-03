# !/bin/sh -eu
cd weex_analyzer
./gradlew clean build test testDebugUnitTestCoverage -Dorg.gradle.daemon=true -Dorg.gradle.parallel=true -Dorg.gradle.jvmargs="-Xmx512m -XX:+HeapDumpOnOutOfMemoryError" -Dfile.encoding=UTF-8
