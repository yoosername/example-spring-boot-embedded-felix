package org.springframework.osgi.context;

import org.osgi.framework.BundleContext;

/**
 * This is a clone of a SpringDM interface to allow us to shim plugins that use it.
 *
 * @deprecated use {@link org.eclipse.gemini.blueprint.context.BundleContextAware}
 */
@Deprecated
public interface BundleContextAware {
    void setBundleContext(BundleContext bundleContext);
}
