package com.example.osgi.framework.web;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;

@Configuration
public class DispatcherSetup extends DispatcherServletAutoConfiguration {
	
	private static final Logger logger = LoggerFactory.getLogger(DispatcherSetup.class);
	
	@Bean
	public DispatcherServlet dispatcherServlet() {
		
	    return new DispatcherServlet() {
	    	
			@Override
			protected void onRefresh(ApplicationContext context) {
				// TODO Auto-generated method stub
				super.onRefresh(context);
				DispatcherSetup.logger.info("SERVLET CONTEXT REFRESHED AS " + context.toString());
			}
	    	
	    };
	}
	
	@Bean
	public ServletContextInitializer contextInitializer() {
	    return new ServletContextInitializer() {

	        @Override
	        public void onStartup(ServletContext servletContext)
	                throws ServletException {
	        	servletContext.setInitParameter("contextClass","com.example.osgi.framework.web.OsgiBundleXmlWebApplicationContext");
	        	DispatcherSetup.logger.info("NEW SERVLET CONTEXT INIALIZED AS = " + servletContext.toString());
	        }
	    };
	}

	/**
	 * Register dispatcherServlet programmatically
	 *
	 * @return ServletRegistrationBean
	 */
	@Bean
	public ServletRegistrationBean<DispatcherServlet> dispatcherServletRegistration() {

	    ServletRegistrationBean<DispatcherServlet> registration = new ServletRegistrationBean<DispatcherServlet>(
	            dispatcherServlet(), 
	            "/dispatch/*"
	    );
	    
	    //registration.addInitParameter("contextClass","com.example.osgi.framework.web.OsgiBundleXmlWebApplicationContext");

	    registration.setName(
	    		DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME
	    );

	    return registration;
	}
	
}
