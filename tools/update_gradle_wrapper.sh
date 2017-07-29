#!/bin/sh

cd "$(dirname $0)/.."
for i in $(find $(pwd) -type f -name gradlew); do
    cd $(dirname $i)
    pwd
    ./gradlew wrapper
    sed -E -i '' s/-bin\\.zip\$/-all.zip/ gradle/wrapper/gradle-wrapper.properties
done
