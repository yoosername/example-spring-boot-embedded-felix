package com.example.osgi;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.apache.felix.framework.FrameworkFactory;
import org.apache.felix.main.AutoProcessor;
import org.osgi.framework.launch.Framework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.example.spring.SpringPropertiesHelper;

@Service	
public class FelixService {
	
	private static final Logger logger = LoggerFactory.getLogger(FelixService.class);
	private static final String SHUTDOWN_HOOK_PROP = "felix.shutdown.hook";
	
	private Framework framework = null;
	
	@Autowired
	private SpringAwareFelixBundleListener springAwareFelixBundleListener;
	
	@Autowired
	private SpringPropertiesHelper propertyHelper;

	public FelixService() {
		// Start the Felix framework once all the beans have been created
		// So that we get our property values injected and available
	    logger.debug("***** FelixService Component Constructed *****");
	}
	
	@EventListener(ApplicationReadyEvent.class)
	void startFramework() {
		
		// Get Felix properties from Spring Boot config
		HashMap<String, String> frameworkProps = propertyHelper.getProps();
		
		
		logger.info("----------------------------------------------------------");
		logger.info(frameworkProps.toString());
		logger.info("| Felix : Framework STARTING");
		logger.info("| Felix : Loading bundles from ("+frameworkProps.get("felix.auto.deploy.dir").toString()+")");

	    addShutdownHook(framework,frameworkProps);
	    
	    try
	    {
	        // Create an instance and initialise the framework.
	        FrameworkFactory factory = getFrameworkFactory();
	        framework = factory.newFramework(frameworkProps);
	        framework.init();
	        
	        // Use the system bundle context to process the auto-deploy
	        // and auto-install/auto-start properties.
	        AutoProcessor.process(frameworkProps, framework.getBundleContext());
	        
	        // Log Bundle Activations
	        framework.getBundleContext().addBundleListener(springAwareFelixBundleListener);
	        
	        // Start the framework.
	        framework.start();
	        
	        // Wait for framework to stop -- then exit the VM.
	        logger.info("| Felix : Framework STARTED - listening for shutdown hook");
	        logger.info("----------------------------------------------------------");
	        framework.waitForStop(0);
	        System.exit(0);
	    }
	    catch (Exception ex)
	    {
	    	logger.error("| Felix : Framework FAILED TO START - Errors: " + ex);
	    	logger.info("----------------------------------------------------------");
	        ex.printStackTrace();
	        System.exit(0);
	    }
	}

	private void addShutdownHook(Framework framework, HashMap<String, String> frameworkProps) {
		
		// Add a shutdown hook to clean stop the framework.
	    String enableHook = frameworkProps.get(SHUTDOWN_HOOK_PROP);
	    if ((enableHook == null) || !enableHook.equalsIgnoreCase("false"))
	    {
	        Runtime.getRuntime().addShutdownHook(new Thread("Felix Shutdown Hook") {
	            public void run()
	            {
	            	logger.info("");
	            	logger.info("| Felix : Framework STOPPING - shutdown hook detected");
	            	logger.info("");
	                try
	                {
	                    if (framework != null)
	                    {
	                    	framework.stop();
	                    	framework.waitForStop(0);
	                    	logger.info("| Felix : Framework STOPPED");
	                    }
	                }
	                catch (Exception ex)
	                {
	                    logger.error("| Felix : Error stopping framework - Errors: " + ex);
	                }
	            }
	        });
	    }
	    
	}
	
	private FrameworkFactory getFrameworkFactory() throws Exception
    {
        java.net.URL url = FelixService.class.getClassLoader().getResource(
            "META-INF/services/org.osgi.framework.launch.FrameworkFactory"
        );
        if (url != null)
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            try
            {
                for (String s = br.readLine(); s != null; s = br.readLine())
                {
                    s = s.trim();
                    // Try to load first non-empty, non-commented line.
                    if ((s.length() > 0) && (s.charAt(0) != '#'))
                    {
                        return (FrameworkFactory) Class.forName(s).newInstance();
                    }
                }
            }
            finally
            {
                if (br != null) br.close();
            }
        }

        throw new Exception("Could not find framework factory.");
    }

}