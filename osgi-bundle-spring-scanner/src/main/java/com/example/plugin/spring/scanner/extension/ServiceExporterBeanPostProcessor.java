package com.example.plugin.spring.scanner.extension;

import java.util.Hashtable;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.osgi.service.exporter.support.AutoExport;

import com.example.plugin.spring.scanner.annotation.export.ExportAsService;

/**
 * A BeanPostProcessor that exports OSGi services for beans annotated with both a *Component annotation and the ExportAsService annotation
 * This essentially does the same thing as the "public=true" on an atlassian plugin.xml component entry.
 * <p/>
 * This is implemented as a BeanPostProcessor because we need to service to come and go as the bean is created/destroyed
 */
public class ServiceExporterBeanPostProcessor implements DestructionAwareBeanPostProcessor
{

    public static final String OSGI_SERVICE_SUFFIX = "_osgiService";
    
    private final ExportedSeviceManager serviceManager;
    private final BundleContext bundleContext;
    private ConfigurableListableBeanFactory beanFactory;

    public ServiceExporterBeanPostProcessor(BundleContext bundleContext,ConfigurableListableBeanFactory beanFactory)
    {
        this.bundleContext = bundleContext;
        this.beanFactory = beanFactory;
        this.serviceManager = new ExportedSeviceManager();
    }

    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException
    {

        if(isPublicComponent(bean))
        {
            String serviceName = getServiceName(beanName);
            serviceManager.unregisterService(bundleContext, bean);

            if(beanFactory.containsBean(serviceName))
            {
                Object serviceBean = beanFactory.getBean(serviceName);

                if(null != serviceBean)
                {
                    beanFactory.destroyBean(serviceName,serviceBean);
                }
            }
        }
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException
    {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException
    {
        Class<?>[] interfaces;

        if (isPublicComponent(bean))
        {
            String serviceName = getServiceName(beanName);
            AutoExport autoExport = AutoExport.DISABLED;
            interfaces = bean.getClass().getAnnotation(ExportAsService.class).value();

            //if they didn't specify any interfaces, calculate them
            if (interfaces.length < 1)
            {
                interfaces = bean.getClass().getInterfaces();
                
                //if we still don't have any, just export with the classname (yes, OSGi allows this.
                if (interfaces.length < 1)
                {
                    interfaces = new Class<?>[]{bean.getClass()};
                }
            }

            try
            {
                ServiceRegistration reg = serviceManager.registerService(bundleContext, bean, beanName, new Hashtable<String, Object>(), autoExport, interfaces);
                beanFactory.initializeBean(reg, serviceName);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return bean;
    }
    
    private boolean isPublicComponent(Object bean) 
    {
        return bean.getClass().isAnnotationPresent(ExportAsService.class);
    }

    private String getServiceName(String beanName)
    {
        return beanName + OSGI_SERVICE_SUFFIX;
    }

}
