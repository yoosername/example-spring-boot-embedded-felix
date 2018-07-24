package org.springframework.osgi.example;

import org.eclipse.gemini.blueprint.context.support.OsgiBundleXmlApplicationContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.QualifierAnnotationAutowireCandidateResolver;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.osgi.context.BundleContextAware;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Application context that initializes the bean definition reader to not validate via XML Schema.  Note that by
 * turning this off, certain defaults won't be populated like expected.  For example, XML Schema provides the default
 * autowire value of "default", but without this validation, that value is not set so autowiring will be turned off.
 *
 * This class exists in the same package as the parent so the log messages won't get confused as the parent class
 * logs against the instance class.
 *
 * @since 2.5.0
 */
public class NonValidatingOsgiBundleXmlApplicationContext extends OsgiBundleXmlApplicationContext {
    public NonValidatingOsgiBundleXmlApplicationContext(final String[] configLocations) {
        super(configLocations);
    }

    @Override
    protected void initBeanDefinitionReader(final XmlBeanDefinitionReader beanDefinitionReader) {
        super.initBeanDefinitionReader(beanDefinitionReader);
        beanDefinitionReader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_NONE);
        beanDefinitionReader.setNamespaceAware(true);
    }

    @Override
    protected void customizeBeanFactory(final DefaultListableBeanFactory beanFactory) {
        if (Boolean.getBoolean("atlassian.disable.spring.cache.bean.metadata")) {
            beanFactory.setCacheBeanMetadata(false);
        }
        if (!Boolean.getBoolean("atlassian.enable.spring.parameter.name.discoverer")) {
            beanFactory.setParameterNameDiscoverer(new ParameterNameDiscoverer() {
                @Override
                public String[] getParameterNames(final Method method) {
                    // We never discover parameter names (for now)
                    return null;
                }

                @Override
                public String[] getParameterNames(final Constructor<?> ctor) {
                    // We never discover parameter names (for now)
                    return null;
                }
            });
        }
        super.customizeBeanFactory(beanFactory);
        beanFactory.addBeanPostProcessor(new ShimSpringDmBundleContextAwareBeanPostProcessor());
        beanFactory.setAutowireCandidateResolver(new QualifierAnnotationAutowireCandidateResolver());
    }

    private class ShimSpringDmBundleContextAwareBeanPostProcessor implements BeanPostProcessor {
        @Override
        public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
            // Inject the BundleContext into beans which mark themselves as needing it.
            if (bean instanceof BundleContextAware) {
                ((BundleContextAware) bean).setBundleContext(getBundleContext());
            }
            return bean;
        }

        @Override
        public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
            // Nothing to do after initialization
            return bean;
        }
    }
}
