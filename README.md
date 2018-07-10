# Example Spring Boot application running embedded Apache Felix OSGI Container

>> 
The idea is to load basic Spring Boot app runing an embedded Felix Framework. 
The Felix framework watches a directory for bundles and loads, unloads, upgrades them
Anything else is handled by Bundles themselves

## Spring boot
- [x] Customise Spring Boot start Banner
- [x] Configure Spring default logging to use log4j2.xml in resources
- [x] Create App Entrypoint as @SpringBootApplication
- [x] Create Event Listener against ApplicationReadyEvent to initialise Felix

## Felix
- [x] Create FelixService as SpringService and provide a method to be called on ApplicationReadyEvent


## Gradle
- [] Create custom start.sh script which sections that can easily be overridden
- [] Create custom Task to copy OSGI Framework bundles into framework_bundles folder
- [] Create an empty external_bundles folder in the distribution
- [] Create zip and tar.gz distributions which include the ./framework_bundles and ./external_bundles folder plus start scripts.