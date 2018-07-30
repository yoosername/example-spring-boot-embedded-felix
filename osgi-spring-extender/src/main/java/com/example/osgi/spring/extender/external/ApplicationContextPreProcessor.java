package com.example.osgi.spring.extender.external;

import org.osgi.framework.Bundle;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Hook to customize the spring application context before it is populated
 *
 * @since 2.6
 */
public interface ApplicationContextPreProcessor {
    /**
     * Detects if this bundle is a Spring bundle.  This allows a pre-processor to have a Spring context built for a bundle
     * even if the bundle doesn't have the Spring context header or any XML files in META-INF/spring.
     *
     * @param bundle The bundle to create an application context for
     * @return True if a spring context should be created, false otherwise
     */
    boolean isSpringPoweredBundle(Bundle bundle);

    /**
     * Process a context before it is populated, usually via adding
     * {@link org.springframework.beans.factory.config.BeanFactoryPostProcessor} instances.
     *
     * @param bundle             The target bundle
     * @param applicationContext The target application context before population
     */
    void process(Bundle bundle, ConfigurableApplicationContext applicationContext);
}
