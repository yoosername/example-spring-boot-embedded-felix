package com.example.osgi.spring.scanner.extension;

import java.util.Hashtable;
import java.util.Map;

import org.eclipse.gemini.blueprint.service.exporter.support.ExportContextClassLoaderEnum;
import org.eclipse.gemini.blueprint.service.exporter.support.OsgiServiceFactoryBean;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Utility class to encapsulate the registration/de-registration of OSGi services exported by public components
 */
public class ExportedSeviceManager
{
    private final Hashtable<Integer,OsgiServiceFactoryBean> exporters;

    public ExportedSeviceManager() {
        this.exporters = new Hashtable<Integer, OsgiServiceFactoryBean>();
    }

    /**
     * Exports a component as an OSGi service for use by other bundles
     * @param bundleContext
     * @param bean
     * @param beanName
     * @param serviceProps
     * @param autoExport
     * @param interfaces
     * @return
     * @throws Exception
     */
    public ServiceRegistration<?> registerService(final BundleContext bundleContext, final Object bean, final String beanName, final Map<String, Object> serviceProps, final Class<?>... interfaces) throws Exception {
        
    	serviceProps.put("org.springframework.osgi.bean.name",beanName);
        
    	OsgiServiceFactoryBean osgiExporter = createExporter(bundleContext,bean,beanName,serviceProps,interfaces);
        
        int hashCode = System.identityHashCode(bean);
        exporters.put(hashCode,osgiExporter);

        ServiceRegistration<?> reg = (ServiceRegistration<?>) osgiExporter.getObject();

        return reg;
    }

    /**
     * de-registers an OSGi service
     * @param bundleContext
     * @param bean
     */
    public void unregisterService(BundleContext bundleContext, Object bean) {
        int hashCode = System.identityHashCode(bean);
        OsgiServiceFactoryBean exporter = exporters.get(hashCode);
        if(null != exporter)
        {
            exporter.destroy();
            exporters.remove(hashCode);
        }
    }

    /**
     * creates the OsgiServiceFactoryBean used by spring when registering services
     */
    private OsgiServiceFactoryBean createExporter(final BundleContext bundleContext, final Object bean, final String beanName, final Map<String, Object> serviceProps, final Class<?>... interfaces) throws Exception {
        
    	OsgiServiceFactoryBean exporter = new OsgiServiceFactoryBean();
        exporter.setBeanClassLoader(bean.getClass().getClassLoader());
        exporter.setBeanName(beanName);
        exporter.setBundleContext(bundleContext);
        exporter.setExportContextClassLoader(ExportContextClassLoaderEnum.UNMANAGED);
        exporter.setInterfaces(interfaces);
        exporter.setServiceProperties(serviceProps);
        exporter.setTarget(bean);
        exporter.afterPropertiesSet();

        return exporter;
    }
}
