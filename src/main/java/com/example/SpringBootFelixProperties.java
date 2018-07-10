package com.example;

import java.util.HashMap;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
* Provides Felix properties from Spring Config
**/
@Component
public class SpringBootFelixProperties implements FelixProperties {

	private static final Logger logger = LoggerFactory.getLogger(SpringBootFelixProperties.class);
	
	@Value("${felix.auto.deploy.dir}")
	private String autoDeployDir;
		
	@Value("${felix.auto.deploy.action}")
	private String autoDeployAction;
	
	@Value("${felix.shutdown.hook}")
	private String shutdownHook;
	
	@Value("${org.osgi.framework.storage.clean}")
	private String osgiStorageClean;
	
	@Value("${felix.fileinstall.dir}")
	private String fileInstallDir;
	
	@Value("${felix.fileinstall.bundles.new.start}")
	private String fileInstallBundlesNewStart;
			
	@Value("${felix.fileinstall.bundles.updateWithListeners}")
	private String fileInstallBundlesUpdateWithListeners;
	
	private Properties props;
	private boolean loaded = false;
	
	public SpringBootFelixProperties() {
	}
	
	@EventListener(ApplicationReadyEvent.class)
	public void loadProps() {
		
		props = new Properties();
		
		logger.info("Loading Felix Properties from Spring properties file");
		
		if(autoDeployDir != null) {
			props.put("felix.auto.deploy.dir", autoDeployDir);
		}
		if(autoDeployAction != null) {
			props.put("felix.auto.deploy.action", autoDeployAction);
		}
		if(shutdownHook != null) {
			props.put("felix.shutdown.hook", shutdownHook);
		}
		if(osgiStorageClean != null) {
			props.put("org.osgi.framework.storage.clean", osgiStorageClean);
		}
		if(fileInstallDir != null) {
			props.put("felix.fileinstall.dir", fileInstallDir);
		}
		if(fileInstallBundlesNewStart != null) {
			props.put("felix.fileinstall.bundles.new.start", fileInstallBundlesNewStart);
		}
		if(fileInstallBundlesUpdateWithListeners != null) {
			props.put("felix.fileinstall.bundles.updateWithListeners", fileInstallBundlesUpdateWithListeners);
		}
		loaded = true;
		
	}
	
	@Override
	public Properties getProperties() {
		if(props == null && !loaded) loadProps();
		return props;
	}

	public HashMap<String, String> getHashMap() {
		
		Properties props = getProperties();
		HashMap<String, String> map = new HashMap<String, String>();
		
		for (final String name: props.stringPropertyNames()) {
			map.put(name, props.getProperty(name));
		}
		
		return map;
	}

}
