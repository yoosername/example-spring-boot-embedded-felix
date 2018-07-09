# Example Spring Boot application running embedded Apache Felix OSGI Container

# Stuff To Do:

## Spring boot

* Custom Banner
* Setup logging (Logback)
* Setup profiles and auto config
* Create service to start Felix and have it run at container READY

## Distribution

* Create custom start.sh script


## Gradle Setup

* Copy OSGI framework dependency bundles to /framework_bundles
* Create a plugins folder at /plugins
* Copy ./scripts/start.sh to ./build/assembly/start.sh
* Compile and build spring boot fat Jar
* Assemble JAR and associated libs
* Create ZIP and tar.gz distributions from /build/assembly folder
