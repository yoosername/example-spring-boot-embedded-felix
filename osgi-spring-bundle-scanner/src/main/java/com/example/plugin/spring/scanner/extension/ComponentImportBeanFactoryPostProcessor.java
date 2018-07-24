package com.example.plugin.spring.scanner.extension;

import static com.example.plugin.spring.scanner.util.AnnotationIndexReader.readAllIndexFilesForProduct;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.osgi.service.importer.support.OsgiServiceProxyFactoryBean;

import com.example.plugin.spring.scanner.util.ClassIndexFiles;

/**
 * This class is run after all of the bean definitions have been gathered for the current bundle.
 * It looks for any of the *Import annotations and registers the proper OSGi imports.
 * This is a BeanFactoryPostProcessor because it needs to run before the beans are created
 * so that the services are available when spring wires up the beans.
 */
public class ComponentImportBeanFactoryPostProcessor implements BeanFactoryPostProcessor
{
    private final BundleContext bundleContext;

    public ComponentImportBeanFactoryPostProcessor(BundleContext bundleContext)
    {
        this.bundleContext = bundleContext;
    }

    /**
     * Reads the componentimport inex file(s) and registers the bean wrappers that represent OSGi import services
     * @param beanFactory
     * @throws BeansException
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException
    {
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;

        Bundle bundle = bundleContext.getBundle();
        List<String> classNames = readAllIndexFilesForProduct(ClassIndexFiles.COMPONENT_IMPORT_INDEX_FILE, bundle);

        for (String className : classNames)
        {
            String[] typeAndName = StringUtils.split(className, "#");
            String beanType = typeAndName[0];
            String beanName = (typeAndName.length > 1) ? typeAndName[1] : "";

            try
            {
                Class beanClass = beanFactory.getBeanClassLoader().loadClass(beanType);
                registerComponentImportBean(registry, beanClass, beanName);
            }
            catch (ClassNotFoundException e)
            {
                //ignore
            }
        }
    }

    /**
     * Figures out the proper bean name for the service and registers it
     * @param registry
     * @param paramType
     * @param beanName
     */
    private void registerComponentImportBean(BeanDefinitionRegistry registry, Class paramType, String beanName)
    {
        String serviceBeanName = beanName;

        if ("".equals(serviceBeanName))
        {

            serviceBeanName = StringUtils.uncapitalize(paramType.getSimpleName());
        }

        registerBeanDefinition(registry, serviceBeanName, "", paramType);
    }

    /**
     * Creates an OsgiServiceProxyFactoryBean for the requested import type.
     * 
     * @param registry
     * @param beanName
     * @param filter
     * @param interfaces
     */
    private void registerBeanDefinition(BeanDefinitionRegistry registry, String beanName, String filter, Class interfaces)
    {

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(OsgiServiceProxyFactoryBean.class);
        builder.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR);
        builder.setLazyInit(true);

        if(StringUtils.isNotBlank(filter))
        {
            builder.addPropertyValue("filter", filter);
        }

        long timeout = 1000;

        builder.addPropertyValue("interfaces", new Class[]{interfaces});
        registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
    }


}
