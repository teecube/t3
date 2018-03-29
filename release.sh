#!/bin/sh

# retrieve Maven auto release script
curl -s https://raw.githubusercontent.com/debovema/maven-auto-releaser/v1.1.0/maven-auto-release.sh -o /tmp/maven-auto-release.sh
chmod u+x /tmp/maven-auto-release.sh
. /tmp/maven-auto-release.sh
