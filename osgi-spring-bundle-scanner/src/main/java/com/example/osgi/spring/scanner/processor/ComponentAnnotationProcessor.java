package com.example.osgi.spring.scanner.processor;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;

import com.example.osgi.spring.scanner.util.ClassIndexFiles;

/**
 * Handles spring's @Component and our product specific *Component annotations and creates class index files for them.
 * Cross product components are listed in META-INF/plugin-components/components
 * Product specific comppnents are listed in META-INF/plugin-components/components-${productName}
 * 
 * Entries in these files are either just the fully qualified class name of the component, or the fully qualified classname
 * plus the bean name defined as the value of the annotation separated by #
 * 
 * Example:
 * 
 * com.some.component.without.a.name.MyClass
 * com.some.component.with.a.name.MyClass#myBeanName
 */
@SupportedAnnotationTypes({"org.springframework.stereotype.Component", "javax.inject.Named", "com.atlassian.plugin.spring.scanner.annotation.component.*"})
public class ComponentAnnotationProcessor extends IndexWritingAnnotationProcessor
{
    public static final String SPRING_COMPONENT_ANNOTATION = "org.springframework.stereotype.Component";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
    {
        doProcess(annotations,roundEnv,ClassIndexFiles.COMPONENT_KEY);
        
        return false;
    }

    @Override
    public TypeAndAnnotation getTypeAndAnnotation(Element element, TypeElement anno)
    {
        TypeElement typeElement = null;
        Annotation annotation = null;
        TypeAndAnnotation typeAndAnnotation = null;
        Class componentAnnoClass;
        try
        {
            componentAnnoClass = Class.forName(anno.getQualifiedName().toString());
        }
        catch (ClassNotFoundException e)
        {
            return typeAndAnnotation;
        }

        if (element instanceof TypeElement)
        {
            typeElement = (TypeElement) element;
            annotation = typeElement.getAnnotation(componentAnnoClass);

            typeAndAnnotation = new TypeAndAnnotation(typeElement, annotation);
        }
        else if (element instanceof VariableElement)
        {
            VariableElement variableElement = (VariableElement) element;
            if (ElementKind.PARAMETER.equals(variableElement.getKind()) && ElementKind.CONSTRUCTOR.equals(variableElement.getEnclosingElement().getKind()))
            {
                typeElement = (TypeElement) processingEnv.getTypeUtils().asElement(variableElement.asType());
                annotation = variableElement.getAnnotation(componentAnnoClass);
                typeAndAnnotation = new TypeAndAnnotation(typeElement, annotation);
            }
        }

        if(null != typeElement && !ElementKind.CLASS.equals(typeElement.getKind()))
        {
            String message = "Annotation processor found a type [" + typeAndAnnotation.getTypeElement().getQualifiedName().toString() + "] annotated as a component, but the type is not a concrete class. NOT adding to index file!!";
            processingEnv.getMessager().printMessage(Diagnostic.Kind.MANDATORY_WARNING,message);

            typeAndAnnotation = null;
        }

        if(null != typeElement && typeElement.getModifiers().contains(Modifier.ABSTRACT))
        {
            String message = "Annotation processor found a type [" + typeAndAnnotation.getTypeElement().getQualifiedName().toString() + "] annotated as a component, but the type is abstract. NOT adding to index file!!";
            processingEnv.getMessager().printMessage(Diagnostic.Kind.MANDATORY_WARNING,message);

            typeAndAnnotation = null;
        }
        
        return typeAndAnnotation;
    }
}
