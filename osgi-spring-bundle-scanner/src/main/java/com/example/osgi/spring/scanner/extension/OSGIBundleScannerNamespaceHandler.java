package com.example.osgi.spring.scanner.extension;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * This class is simply the entry point for the spring xsd extension.
 * It maps the scan-indexes element in the xml to the proper parser
 */
public class OSGIBundleScannerNamespaceHandler extends NamespaceHandlerSupport
{

    @Override
    public void init()
    {
        registerBeanDefinitionParser("scan-indexes",new OSGIBundleScannerBeanDefinitionParser());
    }
}
