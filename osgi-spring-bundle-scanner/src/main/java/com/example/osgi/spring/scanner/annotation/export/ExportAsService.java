package com.example.osgi.spring.scanner.annotation.export;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes a component to be exported as an OSGi service available to other bundles.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExportAsService
{
    /**
     * the interfaces the service should be exported as.
     * if not specified, the interfaces will be calculated using reflection
     * @return
     */
    Class<?>[] value() default {};
}
