package com.example.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;

@Configuration
public class DispatcherSetup extends DispatcherServletAutoConfiguration {
	
	@Bean
	public DispatcherServlet dispatcherServlet() {
	    return new DispatcherServlet();
	}
	
	@Bean
	public ServletContextInitializer contextInitializer() {
	    return new ServletContextInitializer() {

	        @Override
	        public void onStartup(ServletContext servletContext)
	                throws ServletException {
	                servletContext.setInitParameter("contextClass","org.springframework.osgi.web.context.support.OsgiBundleXmlWebApplicationContext");
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

	    registration.setName(
	    		DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME
	    );

	    return registration;
	}
	
}
