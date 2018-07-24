package com.example.plugin.spring.scanner.extension;

import java.beans.Introspector;
import java.util.*;

import com.example.plugin.spring.scanner.util.ClassIndexFiles;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.util.ClassUtils;

import static com.example.plugin.spring.scanner.util.AnnotationIndexReader.readAllIndexFilesForProduct;
import static com.example.plugin.spring.scanner.util.AnnotationIndexReader.readIndexFile;

/**
 * This class is responsible for reading the class index files and generating bean definitions from them.
 * We assume all of the proper type checks/visibility checks have been done by the annotation processors.
 * This means that if a class is listed in an index, it qualifies as a bean candidate.
 */
public class ClassIndexBeanDefinitionScanner
{
    protected final Log logger = LogFactory.getLog(getClass());

    private BeanDefinitionRegistry registry;

    public ClassIndexBeanDefinitionScanner(BeanDefinitionRegistry registry)
    {
        this.registry = registry;
    }

    /**
     * Gets the map of beanName -> beanDefinition and returns a set of bean definition holders
     * @return
     */
    protected Set<BeanDefinitionHolder> doScan()
    {

        Set<BeanDefinitionHolder> beanDefinitions = new LinkedHashSet<BeanDefinitionHolder>();
        Map<String, BeanDefinition> namesAndDefinitions = findCandidateComponents();

        for (Map.Entry<String, BeanDefinition> nameAndDefinition : namesAndDefinitions.entrySet())
        {

            if (checkCandidate(nameAndDefinition.getKey(), nameAndDefinition.getValue()))
            {
                BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(nameAndDefinition.getValue(), nameAndDefinition.getKey());
                beanDefinitions.add(definitionHolder);
                registerBeanDefinition(definitionHolder, registry);
            }
        }
        return beanDefinitions;
    }


    /**
     * Reads the components from the index file(s) and generates a map of beanName -> beanDfinitions for them
     * @return
     */
    public Map<String, BeanDefinition> findCandidateComponents()
    {
        Map<String, BeanDefinition> candidates = new HashMap<String, BeanDefinition>();

        List<String> beanTypeAndNames = readAllIndexFilesForProduct(ClassIndexFiles.COMPONENT_INDEX_FILE, Thread.currentThread().getContextClassLoader());

        for (String beanTypeAndName : beanTypeAndNames)
        {
            String[] typeAndName = StringUtils.split(beanTypeAndName, "#");

            String beanClassname = typeAndName[0];
            String beanName = "";

            if (typeAndName.length > 1)
            {
                beanName = typeAndName[1];
            }

            if (StringUtils.isBlank(beanName))
            {
                beanName = Introspector.decapitalize(ClassUtils.getShortName(beanClassname));
            }

            candidates.put(beanName, BeanDefinitionBuilder.genericBeanDefinition(beanClassname).getBeanDefinition());
        }

        return candidates;
    }

    /**
     * copyPasta from spring-context:component-scan classes
     */
    protected void registerBeanDefinition(BeanDefinitionHolder definitionHolder, BeanDefinitionRegistry registry)
    {
        BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, registry);
    }

    /**
     * copyPasta from spring-context:component-scan classes
     * 
     * Check the given candidate's bean name, determining whether the corresponding
     * bean definition needs to be registered or conflicts with an existing definition.
     */
    protected boolean checkCandidate(String beanName, BeanDefinition beanDefinition) throws IllegalStateException
    {
        if (!this.registry.containsBeanDefinition(beanName))
        {
            return true;
        }

        BeanDefinition existingDef = this.registry.getBeanDefinition(beanName);
        BeanDefinition originatingDef = existingDef.getOriginatingBeanDefinition();
        if (originatingDef != null)
        {
            existingDef = originatingDef;
        }
        if (isCompatible(beanDefinition, existingDef))
        {
            return false;
        }
        throw new IllegalStateException("Annotation-specified bean name '" + beanName +
                "' for bean class [" + beanDefinition.getBeanClassName() + "] conflicts with existing, " +
                "non-compatible bean definition of same name and class [" + existingDef.getBeanClassName() + "]");
    }

    /**
     * copyPasta from spring-context:component-scan classes
     * 
     * Determine whether the given new bean definition is compatible with
     * the given existing bean definition.
     * <p>The default implementation simply considers them as compatible
     * when the bean class name matches.
     *
     */
    protected boolean isCompatible(BeanDefinition newDefinition, BeanDefinition existingDefinition)
    {
        return (!(existingDefinition instanceof AnnotatedBeanDefinition) ||  // explicitly registered overriding bean
                newDefinition.getSource().equals(existingDefinition.getSource()) ||  // scanned same file twice
                newDefinition.equals(existingDefinition));  // scanned equivalent class twice
    }


}
