package com.example.plugin.spring.scanner.annotation.imports;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * Annotation representing an OSGi service that's required to be imported into this bundle.
 * Can be applied to constructor params where the param type is a service interface exported
 * by another bundle
 */
@Target({ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ComponentImport
{
    String value() default "";
}
