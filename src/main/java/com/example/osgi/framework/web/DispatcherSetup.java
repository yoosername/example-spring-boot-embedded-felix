package com.example.osgi.framework.web;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.servlet.DispatcherServlet;

@Configuration
public class DispatcherSetup implements WebApplicationInitializer {
	
	private static final Logger dslogger = LoggerFactory.getLogger(DispatcherServlet.class);

    @Override
    public void onStartup(ServletContext container) {
    	
    	OsgiBundleXmlWebApplicationContext appContext = new OsgiBundleXmlWebApplicationContext();
    	container.addListener(new ContextLoaderListener(appContext));

    	ServletRegistration.Dynamic dispatcher = container.addServlet("dispatcher", new DispatcherServlet(appContext) {
	    	
			@Override
			protected void onRefresh(ApplicationContext context) {
				super.onRefresh(context);
				DispatcherSetup.dslogger.info("SERVLET CONTEXT REFRESHED AS " + context.toString());
			}
	    	
	    });
    	
    	dispatcher.setLoadOnStartup(1);
    	dispatcher.addMapping("/dispatch");
    	
    }

 }