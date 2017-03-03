# !/bin/sh -eu
cd playground
./gradlew clean build test :weex_analyzer:testDebugUnitTestCoverage -Dorg.gradle.daemon=true -Dorg.gradle.parallel=true -Dorg.gradle.jvmargs="-Xmx512m -XX:+HeapDumpOnOutOfMemoryError" -Dfile.encoding=UTF-8
