# Example Spring Boot web application running embedded Apache Felix OSGI Container

>> 
The idea is to be able to:
* load basic Spring Boot App running an embedded Felix Framework.
* Add functionality using plain jars using standard annotations like Spring Web controllers and ServletFilter etc
* Auto detect and serve embedded static resources


## Spring boot
- [x] Create App Entrypoint as @SpringBootApplication
- [x] Customise Spring Boot start Banner
- [x] Configure Spring default logging to use log4j2.xml in resources
- [x] Add a way for Felix framework to use Spring boot properties in internal environment

## Felix
- [x] Create FelixService as Spring Service Component and ApplicationReadyEvent Event Listener to initialise it
- [x] Configure Felix to load some predefined framework bundles


## Gradle
- [] Generate custom start.sh script in output distribution
- [x] Create Task to copy configurable OSGI Framework bundles into framework_bundles folder in distribution
- [] Create an empty plugins folder in the distribution
- [] Create zip and tar.gz distributions which include the ./framework_bundles and ./plugins folder plus start scripts.

## Additional Custom Bundles for initial framework load
- [] PluginServices 
	- Services for turning plain JAR into OSGI bundle using BND tools
	- OSGI bundle activation processing pipeline with hooks for custom processing e.g.
		- Detecting and wiring Servlets, Filters via annotations
		- Detecting and wiring Spring MVC Controllers, RestControllers etc
		- Detecting and wiring custom @AuthRequired annotations etc
		- Detecting static resources
	
- [] PluginFileWatcherService - watch a dir for new jars then use PluginServices to process it.