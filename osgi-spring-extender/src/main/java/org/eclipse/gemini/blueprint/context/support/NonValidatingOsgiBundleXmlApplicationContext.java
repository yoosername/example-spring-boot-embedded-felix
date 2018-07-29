package org.eclipse.gemini.blueprint.context.support;

import org.eclipse.gemini.blueprint.context.BundleContextAware;
import org.eclipse.gemini.blueprint.context.support.OsgiBundleXmlApplicationContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.QualifierAnnotationAutowireCandidateResolver;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Application context that initializes the bean definition reader to not validate via XML Schema.  Note that by
 * turning this off, certain defaults won't be populated like expected.  For example, XML Schema provides the default
 * autowire value of "default", but without this validation, that value is not set so autowiring will be turned off.
 *
 * This class exists in the same package as the parent so the log messages won't get confused as the parent class
 * logs against the instance class.
 */
public class NonValidatingOsgiBundleXmlApplicationContext extends OsgiBundleXmlApplicationContext {
	
    public NonValidatingOsgiBundleXmlApplicationContext(final String[] configLocations) {
        super(configLocations);
    }

    @Override
    protected void initBeanDefinitionReader(final XmlBeanDefinitionReader beanDefinitionReader) {
        super.initBeanDefinitionReader(beanDefinitionReader);
        
        // Don't validate XML.
        beanDefinitionReader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_NONE);
        // Because not validating this is required
        beanDefinitionReader.setNamespaceAware(true);
    }

    @Override
    protected void customizeBeanFactory(final DefaultListableBeanFactory beanFactory) {
        super.customizeBeanFactory(beanFactory);
        
        
        // TODO: Maybe take this back out?!?!?
        beanFactory.createBean(RequestMappingHandlerMapping.class);
        beanFactory.addBeanPostProcessor(new BundleContextAwareBeanPostProcessor());
        beanFactory.setAutowireCandidateResolver(new QualifierAnnotationAutowireCandidateResolver());
    }

    /**
     * If bean is BundleContextAware then we need to inject the bundle context here.
     */
    private class BundleContextAwareBeanPostProcessor implements BeanPostProcessor {
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
