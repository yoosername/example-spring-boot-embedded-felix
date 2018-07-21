package com.example.spring;

import java.util.HashMap;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;

@Component
public class SpringPropertiesHelper {
	
	private static final Logger logger = LoggerFactory.getLogger(SpringPropertiesHelper.class);
	private HashMap<String, String> props;
	private boolean loaded = false;
	
	@Autowired
	ApplicationContext ctx;
	
	public SpringPropertiesHelper(){
		this.props = new HashMap<String,String>();
	}

	public HashMap<String, String> getProps(){
		if(!loaded) {
			loadProperties();
		}
		return this.props;
	}
	
	@PostConstruct
	private void loadProperties() {
		Environment env = ctx.getEnvironment();
		if (ctx.getEnvironment() instanceof ConfigurableEnvironment) {
			// Get all Spring detected Property sources
			MutablePropertySources sources = ((ConfigurableEnvironment) env).getPropertySources();
			MutablePropertySources filteredSources = new MutablePropertySources();
			sources.iterator().forEachRemaining(s -> {
				
				// If its from a classpath .properties file
				if(s.getClass().isAssignableFrom(OriginTrackedMapPropertySource.class)) {
					// only load props if source matches active profile
					if(s.getName().toString().contains("application") && env.acceptsProfiles("prod")) {
						filteredSources.addLast(s);
					}else if(s.getName().toString().contains("application-dev") && env.acceptsProfiles("dev")) {
						filteredSources.addLast(s);
					}
				}else {
					filteredSources.addLast(s);
				}
				
				logger.info(s.getName().toString());
				
			});
			for (PropertySource<?> propertySource : filteredSources ) {
				if (propertySource instanceof EnumerablePropertySource) {
					for (String key : ((EnumerablePropertySource) propertySource).getPropertyNames()) {
						props.put(key, (String)propertySource.getProperty(key));
					}
				}
			}
		}
		loaded = true;
	}

}
