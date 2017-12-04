# How to contribute ?

## Set up an Eclipse workspace with teecube projects

Using Eclipse Oomph

## Generate the documentation locally

### Install a local Sonatype Nexus repository

Considering Docker is installed on the local machine and a Docker machine called
*default* exists and is reachable, simply install a local Sonatype Nexus
repository by executing this script :

```sh
sh -c "$(curl -fsSL https://git.teecu.be/teecube/helpers/raw/master/local-nexus/createLocalNexus.sh)"
```

### Deploy documentation

Using Maven