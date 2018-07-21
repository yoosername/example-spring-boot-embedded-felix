package com.example.spring;

import java.io.IOException;

import org.springframework.beans.factory.config.BeanDefinition;

public interface DynamicControllerRegistry extends DynamicBeanDefinitionRegistry {


	public void registerController(Class<?> controllerClass);
    public void registerController(Class<?> controllerClass, String scope); 
    public void registerController(Class<?> controllerClass, String scope, boolean lazyInit); 
    public void registerController(Class<?> controllerClass, String scope, boolean lazyInit,boolean autowireCandidate); 
	public void registerController(String beanName,Class<?> controllerClass);
    public void registerController(String beanName,Class<?> controllerClass, String scope); 
    public void registerController(String beanName,Class<?> controllerClass, String scope, boolean lazyInit); 
    public void registerController(String beanName,Class<?> controllerClass, String scope, boolean lazyInit,boolean autowireCandidate); 
    public void registerController(String beanName, BeanDefinition beanDefinition);
    public void removeController(String controllerBeanName) throws IOException;
	public void registerGroovyController(String scriptLocation) throws IOException;
	public void removeGroovyController(String scriptLocation,String controllerBeanName) throws IOException;
	
}