# !/bin/sh -eu

./gradlew clean -Dorg.gradle.daemon=true -Dorg.gradle.parallel=true -Dorg.gradle.jvmargs="-Xmx512m -XX:+HeapDumpOnOutOfMemoryError" -Dfile.encoding=UTF-8
