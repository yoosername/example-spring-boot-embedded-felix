package com.example.spring;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;

public interface DynamicBeanDefinitionRegistry extends BeanDefinitionRegistry {

    public void registerBean(Class<?> beanClass);
    public void registerBean(Class<?> beanClass, String scope); 
    public void registerBean(Class<?> beanClass, String scope, boolean lazyInit); 
    public void registerBean(Class<?> beanClass, String scope, boolean lazyInit,boolean autowireCandidate); 
    public void registerBean(String beanName, Class<?> beanClass);
    public void registerBean(String beanName, Class<?> beanClass, String scope); 
    public void registerBean(String beanName, Class<?> beanClass, String scope, boolean lazyInit); 
    public void registerBean(String beanName, Class<?> beanClass, String scope, boolean lazyInit,boolean autowireCandidate); 
 
}