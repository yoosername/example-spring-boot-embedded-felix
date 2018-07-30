package com.example.osgi.framework.web;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.Resource;
import org.eclipse.gemini.blueprint.context.ConfigurableOsgiBundleApplicationContext;
import org.eclipse.gemini.blueprint.context.support.OsgiBundleXmlApplicationContext;
import org.eclipse.gemini.blueprint.extender.OsgiBeanFactoryPostProcessor;
import org.springframework.ui.context.Theme;
import org.springframework.ui.context.ThemeSource;
import org.springframework.ui.context.support.UiApplicationContextUtils;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ServletConfigAware;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.support.AbstractRefreshableWebApplicationContext;
import org.springframework.web.context.support.ServletContextAwareProcessor;
import org.springframework.web.context.support.ServletContextResourcePatternResolver;
import org.springframework.web.context.support.StandardServletEnvironment;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * <code>ServerOsgiBundleXmlWebApplicationContext</code> is a custom extension of
 * {@link OsgiBundleXmlApplicationContext} which provides support for application contexts backed by an OSGi
 * {@link Bundle bundle} in Spring MVC based web applications by implementing {@link ConfigurableWebApplicationContext}.
 * <p />
 * 
 * Since Java does not support multiple inheritance, the implementation details specific to
 * <code>ConfigurableWebApplicationContext</code> have been copied directly from {@link XmlWebApplicationContext} and
 * {@link AbstractRefreshableWebApplicationContext}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is not thread-safe.
 * 
 */
public class OsgiBundleXmlWebApplicationContext extends OsgiBundleXmlApplicationContext implements ConfigurableWebApplicationContext,
    ThemeSource {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * {@link ServletContext} attribute name for the {@link BundleContext} to be used to back this
     * {@link org.eclipse.gemini.blueprint.context.ConfigurableOsgiBundleApplicationContext
     * ConfigurableOsgiBundleApplicationContext}.
     */
    public static final String BUNDLE_CONTEXT_ATTRIBUTE = "osgi-bundlecontext";

    /** service entry used for storing the namespace associated with this context */
    private static final String APPLICATION_CONTEXT_SERVICE_NAMESPACE_PROPERTY = "org.springframework.web.context.namespace";

    /** Suffix for WebApplicationContext namespaces. */
    private static final String DEFAULT_NAMESPACE_SUFFIX = "-servlet";

    private static final String PREFIX_DELIMITER = ":";

    /** Servlet context that this context runs in */
    private ServletContext servletContext;
    
    /** ResourcePatternResolver for the associated ServletContext. */
    private ServletContextResourcePatternResolver servletContextResourcePatternResolver;

    /** Servlet config that this context runs in, if any */
    private ServletConfig servletConfig;

    /** Namespace of this context, or <code>null</code> if root */
    private String namespace;

    /** the ThemeSource for this ApplicationContext */
    private ThemeSource themeSource;

    /**
     * Creates a <code>ServerOsgiBundleXmlWebApplicationContext</code> with no parent.
     */
    public OsgiBundleXmlWebApplicationContext() {
        super();
        setDisplayName("Root ServerOsgiBundleXmlWebApplicationContext");
    }

    /**
     * Creates a <code>ServerOsgiBundleXmlWebApplicationContext</code> with the supplied config locations.
     * 
     * @param configLocations the config locations.
     */
    public OsgiBundleXmlWebApplicationContext(String[] configLocations) {
        super(configLocations);
        setDisplayName("Root ServerOsgiBundleXmlWebApplicationContext");
        logger.debug("Creating an ServerOsgiBundleXmlWebApplicationContext with locations [{}].", ObjectUtils.nullSafeToString(configLocations));
    }

    /**
     * Creates a <code>ServerOsgiBundleXmlWebApplicationContext</code> with the supplied parent.
     * 
     * @param parent the parent {@link ApplicationContext}.
     */
    public OsgiBundleXmlWebApplicationContext(ApplicationContext parent) {
        super(parent);
        setDisplayName("Root ServerOsgiBundleXmlWebApplicationContext");
        logger.debug("Creating an ServerOsgiBundleXmlWebApplicationContext with parent [{}].", parent);
    }

    /**
     * Creates a <code>ServerOsgiBundleXmlWebApplicationContext</code> with the supplied parent and config locations.
     * 
     * @param configLocations the config locations.
     * @param parent the parent {@link ApplicationContext}.
     */
    public OsgiBundleXmlWebApplicationContext(String[] configLocations, ApplicationContext parent) {
        super(configLocations, parent);
        setDisplayName("Root ServerOsgiBundleXmlWebApplicationContext");
        logger.debug("Creating an ServerOsgiBundleXmlWebApplicationContext with locations [{}] and parent [{}].",
            ObjectUtils.nullSafeToString(configLocations), parent);
    }

    /**
     * Determines if the supplied <code>location</code> does not have a prefix.
     */
    protected static boolean hasNoPrefix(String location) {
        return location == null || location.indexOf(PREFIX_DELIMITER) < 1;
    }

    /**
     * {@inheritDoc}
     */
    public void setServletContext(final ServletContext servletContext) {

        this.servletContext = servletContext;
        this.servletContextResourcePatternResolver = new ServletContextResourcePatternResolver(servletContext);

        // If the BundleContext has not yet been set, attempt to retrieve it from the ServletContext or the parent
        // ApplicationContext.
        if (getBundleContext() == null) {
	    getBundleContext();
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BundleContext getBundleContext() {
        BundleContext bundleContext = super.getBundleContext();
        if (bundleContext == null) {

            // Attempt to locate the BundleContext in the ServletContext
            if (this.servletContext != null) {
                Object bundleContextFromServletContext = this.servletContext.getAttribute(BUNDLE_CONTEXT_ATTRIBUTE);
                if (bundleContextFromServletContext != null) {
                    Assert.isInstanceOf(BundleContext.class, bundleContextFromServletContext);
                    this.logger.debug("Using the BundleContext stored in the ServletContext as '{}'.", BUNDLE_CONTEXT_ATTRIBUTE);
                    bundleContext = (BundleContext) bundleContextFromServletContext;
                    setBundleContext(bundleContext);
                }
            }

            // If still not set, fall back to the parent
            if (bundleContext == null) {
                ApplicationContext parent = getParent();
                if (parent instanceof ConfigurableOsgiBundleApplicationContext) {
                    this.logger.debug("Using the parent ApplicationContext's BundleContext");
                    bundleContext = ((ConfigurableOsgiBundleApplicationContext) parent).getBundleContext();
                    setBundleContext(bundleContext);
                }
            }
        }
        return bundleContext;
    }


    /**
     * {@inheritDoc}
     */
    public ServletContext getServletContext() {
        return this.servletContext;
    }

    /**
     * {@inheritDoc}
     */
    public void setServletConfig(ServletConfig servletConfig) {
        this.servletConfig = servletConfig;
        if (servletConfig != null) {
            if (getServletContext() == null) {
                setServletContext(servletConfig.getServletContext());
            }
            if (getNamespace() == null) {
                setNamespace(servletConfig.getServletName() + DEFAULT_NAMESPACE_SUFFIX);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public ServletConfig getServletConfig() {
        return this.servletConfig;
    }

    /**
     * Set the config locations for this application context in init-param style, i.e. with distinct locations separated
     * by commas, semicolons or whitespace.
     * <p>
     * If not set, the implementation may use a default as appropriate.
     */
    public void setConfigLocation(String location) {
        setConfigLocations(StringUtils.tokenizeToStringArray(location, CONFIG_LOCATION_DELIMITERS));
    }

    /**
     * The default location for the root context is "/WEB-INF/applicationContext.xml", and "/WEB-INF/test-servlet.xml"
     * for a context with the namespace "test-servlet" (like for a DispatcherServlet instance with the servlet-name
     * "test").
     */
    @Override
    protected String[] getDefaultConfigLocations() {
        String ns = getNamespace();
        if (ns != null) {
            return new String[] { XmlWebApplicationContext.DEFAULT_CONFIG_LOCATION_PREFIX + ns
                + XmlWebApplicationContext.DEFAULT_CONFIG_LOCATION_SUFFIX };
        } else {
            return new String[] { XmlWebApplicationContext.DEFAULT_CONFIG_LOCATION };
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
        if (namespace != null) {
            setDisplayName("ServerOsgiBundleXmlWebApplicationContext for namespace '" + namespace + "'");
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getNamespace() {
        return this.namespace;
    }

    /**
     * Register request/session scopes, a {@link ServletContextAwareProcessor}, etc.
     * 
     * @see WebApplicationContextUtils#registerWebApplicationScopes(ConfigurableListableBeanFactory)
     */
    @Override
    protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        super.postProcessBeanFactory(beanFactory);

	// Drive the kernel's bean factory post processors.
        BundleContext bundleContext = getBundleContext();
        if (bundleContext != null) {
            ServiceReference<OsgiBeanFactoryPostProcessor> sr = bundleContext.getServiceReference(OsgiBeanFactoryPostProcessor.class);
            if (sr != null) {
                OsgiBeanFactoryPostProcessor kernelPostProcessor = bundleContext.getService(sr);
                try {
                    kernelPostProcessor.postProcessBeanFactory(bundleContext, beanFactory);
                } catch (Exception e) {
                    throw new ApplicationContextException("Kernel bean factory post processor failed", e);
                } finally {
                    bundleContext.ungetService(sr);
                }
            }
        }

        beanFactory.addBeanPostProcessor(new ServletContextAwareProcessor(getServletContext(), getServletConfig()));
        beanFactory.ignoreDependencyInterface(ServletContextAware.class);
        beanFactory.ignoreDependencyInterface(ServletConfigAware.class);
        beanFactory.registerResolvableDependency(ServletContext.class, getServletContext());
        beanFactory.registerResolvableDependency(ServletConfig.class, getServletConfig());

        WebApplicationContextUtils.registerWebApplicationScopes(beanFactory);
    }

    /**
     * {@inheritDoc}
     * 
     * Additionally, this implementation publishes the context namespace under the
     * <code>org.springframework.web.context.namespace</code> property.
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void customizeApplicationContextServiceProperties(Map serviceProperties) {
        super.customizeApplicationContextServiceProperties(serviceProperties);
        String ns = getNamespace();
        if (ns != null) {
            serviceProperties.put(APPLICATION_CONTEXT_SERVICE_NAMESPACE_PROPERTY, ns);
        }
    }

    /** 
     * Returns a {@link org.springframework.web.context.support.ServletContextResource ServletContextResource} if the supplied <code>location</code> does not have a prefix and
     * otherwise delegates to the parent implementation for standard Spring DM resource loading semantics.
     * <p/>
     * This override is necessary to return a suitable {@link Resource} type for flow-relative views. See DMS-2310.
     */
    @Override
    public Resource getResource(String location) {
        if (hasNoPrefix(location)) {
            return this.servletContextResourcePatternResolver.getResource(location);
        }
        return super.getResource(location);
    }

    /** 
     * Returns an array of {@link org.springframework.web.context.support.ServletContextResource ServletContextResource}s if the supplied <code>locationPattern</code> does
     * not have a prefix and otherwise delegates to the parent implementation for standard Spring DM resource loading
     * semantics.
     * <p/>
     * This override is necessary to return a suitable {@link Resource} type for flow-relative views. See DMS-2310.
     */
    @Override
    public Resource[] getResources(String locationPattern) throws IOException {
        if (hasNoPrefix(locationPattern)) {
            return this.servletContextResourcePatternResolver.getResources(locationPattern);
        }
        return super.getResources(locationPattern);
    }

    /**
     * Initialize the theme capability.
     */
    @Override
    protected void onRefresh() {
        this.themeSource = UiApplicationContextUtils.initThemeSource(this);
    }

    /**
     * {@inheritDoc}
     */
    public Theme getTheme(String themeName) {
        return this.themeSource.getTheme(themeName);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    protected ConfigurableEnvironment createEnvironment() {
        return new StandardServletEnvironment();
    }

}