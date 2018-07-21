package com.example.servlet;

import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;

import org.springframework.web.servlet.DispatcherServlet;

@WebServlet(name="OSGIAwareSpringPoweredDispatcher",
urlPatterns={"/plugins*"},
initParams={@WebInitParam(
		name="contextClass", 
		value="org.springframework.osgi.web.context.support.OsgiBundleXmlWebApplicationContext"
)})
public class OSGISpringDispatcherServlet extends DispatcherServlet { 
	
}