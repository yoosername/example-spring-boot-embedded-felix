package com.example;

import java.util.HashMap;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;

/**
 * Helper provides Spring Environment properties as Properties or HashMap object
 */
@Component
public class DefaultSpringBootConfig implements SpringBootConfig {

	private static final Logger logger = LoggerFactory.getLogger(SpringBootConfig.class);

	@Autowired
	private ConfigurableEnvironment  springEnvironment;	

	private Properties props;
	private boolean loaded = false;

	public DefaultSpringBootConfig() {
		// Empty Constructor for Spring
	}

	/**
	 * Detect the properties previously loaded into the Environment by Spring and put them into a cache
	 */
	@Override
	@EventListener(ApplicationReadyEvent.class)
	public void loadProps() {

		props = new Properties();

		logger.debug("Detecting Spring Properties in environment");


		if (springEnvironment instanceof ConfigurableEnvironment) {
			for (PropertySource<?> propertySource : ((ConfigurableEnvironment) springEnvironment).getPropertySources()) {
				if (propertySource instanceof EnumerablePropertySource) {
					for (String key : ((EnumerablePropertySource<?>) propertySource).getPropertyNames()) {
						props.put(key, propertySource.getProperty(key));
					}
				}
			}
		}

		logger.debug("Spring Properties : " + props.toString().replaceAll(", ", "\n"));

		loaded = true;

	}

	/**
	 * Return Spring environment properties as Properties object
	 * @return Spring environment properties
	 */
	@Override
	public Properties getProperties() {
		if(props == null && !loaded) loadProps();
		return props;
	}

	/**
	 * Return Spring environment properties as HashMap object
	 * @return Spring environment properties
	 */
	@Override
	public HashMap<String, String> getHashMap() {

		Properties props = getProperties();
		HashMap<String, String> map = new HashMap<String, String>();

		for (final String name: props.stringPropertyNames()) {
			map.put(name, props.getProperty(name));
		}

		return map;
	}

}
