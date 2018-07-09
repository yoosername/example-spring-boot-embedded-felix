package com.example;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
* Provides Felix properties from Spring Config
**/
@Component
public class SpringBootFelixProperties implements FelixProperties {

	@Value("${felix.auto.deploy.dir}")
	private String autoDeployDir;
		
	@Value("${felix.auto.deploy.action}")
	private String autoDeployAction;
	
	private Properties props;
	
	public SpringBootFelixProperties() {
		props = new Properties();
	}
	
	@EventListener(ApplicationReadyEvent.class)
	public void loadProps() {
		
		if(autoDeployDir != null) {
			props.put("felix.auto.deploy.dir", autoDeployDir);
		}
		if(autoDeployAction != null) {
			props.put("felix.auto.deploy.action", autoDeployAction);
		}
		
	}
	
	@Override
	public Properties getProperties() {
		return props;
	}

}
