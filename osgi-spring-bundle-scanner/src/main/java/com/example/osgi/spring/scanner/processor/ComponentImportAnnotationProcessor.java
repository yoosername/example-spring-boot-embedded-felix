package com.example.osgi.spring.scanner.processor;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import com.example.osgi.spring.scanner.util.ClassIndexFiles;

/**
 * Handles *ComponentImport annotations and creates class index files for them.
 * Cross product imports are listed in META-INF/plugin-components/componentimport
 * Product specific imports are listed in META-INF/plugin-components/componentimport-${productName}
 *
 * Entries in these files are either just the fully qualified class name of the component, or the fully qualified classname
 * plus the bean name defined as the value of the annotation separated by #
 *
 * Example:
 *
 * com.some.component.without.a.name.MyClass
 * com.some.component.with.a.name.MyClass#myBeanName
 */
@SupportedAnnotationTypes("com.atlassian.plugin.spring.scanner.annotation.imports.*")
public class ComponentImportAnnotationProcessor extends IndexWritingAnnotationProcessor
{
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
    {

        doProcess(annotations,roundEnv,ClassIndexFiles.COMPONENT_IMPORT_KEY);

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

        if (element instanceof VariableElement)
        {
            VariableElement variableElement = (VariableElement) element;
            if (ElementKind.PARAMETER.equals(variableElement.getKind()) && ElementKind.CONSTRUCTOR.equals(variableElement.getEnclosingElement().getKind()))
            {
                typeElement = (TypeElement) processingEnv.getTypeUtils().asElement(variableElement.asType());
                annotation = variableElement.getAnnotation(componentAnnoClass);
                typeAndAnnotation = new TypeAndAnnotation(typeElement, annotation);
            }
        }

        return typeAndAnnotation;
    }

}
