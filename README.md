# T³

## Release trigger branch

This branch is a release trigger. It means that **whenever a commit is pushed on this branch**, a release job will be launched, based on properties set in *release.properties* file.

## How to trigger a release ?

To trigger a new release of T³, follow these steps:

* checkout this branch (after cloning the repository):
```shell
git checkout release
```

* edit Release Version:
```shell
RELEASE_VERSION=0.0.1 && sed -i "s/\(RELEASE_VERSION=\).*\$/\1${RELEASE_VERSION}/" release.properties
```

* edit Development Version:
```shell
DEV_VERSION=0.0.2-SNAPSHOT && sed -i "s/\(DEV_VERSION=\).*\$/\1${DEV_VERSION}/" release.properties
```

* commit the release information:
```shell
git add release.properties && git commit -m "Triggering release"
```

* trigger the release by pushing to the release branch:
```shell
git push origin release
```
