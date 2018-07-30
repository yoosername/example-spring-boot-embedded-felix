package com.example.osgi.framework.felix;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Component
public class SpringAwareFelixBundleListener implements BundleListener {
	
	private static final Logger logger = LoggerFactory.getLogger(SpringAwareFelixBundleListener.class);
	
	@Autowired
	private ApplicationContext appContext;
	@Autowired RequestMappingHandlerMapping requestMapping;
	
	public SpringAwareFelixBundleListener() {
	}
	
	@EventListener(ApplicationStartedEvent.class)
	public void init() {
		
	}

	@Override
	public void bundleChanged(BundleEvent bundleEvent) {
		
		//if(Bundle.ACTIVE == bundleEvent.getBundle().getState()) {
		//	logger.info(String.format("Bundle %s started - autowiring components & controllers",bundleEvent.getBundle().getSymbolicName()));
		//	registerBeans(bundleEvent.getBundle());
		//}
		logger.info(String.format(
				"Bundle %s changed state to %s", 
				bundleEvent.getBundle().getSymbolicName(), 
				getBundleStateAsString(bundleEvent.getBundle().getState()
		)));
		
	}
	
	private void registerBeans(Bundle bundle) {
		// Find all beans
		// Add them to the applicationContext
		
	}

	private String getBundleStateAsString(int state) {
		
		switch(state) {
			case Bundle.ACTIVE: return "Active";
			case Bundle.INSTALLED: return "Installed";
			case Bundle.RESOLVED: return "Resolved";
			case Bundle.STARTING: return "Starting";
			case Bundle.STOPPING: return "Stopping";
			case Bundle.UNINSTALLED: return "Uninstalled";
			default: return "Unknown";
		}
		
	}

}
