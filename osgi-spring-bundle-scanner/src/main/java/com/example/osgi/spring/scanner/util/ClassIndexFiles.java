package com.example.osgi.spring.scanner.util;

public class ClassIndexFiles
{
    public static final String INDEX_FILES_DIR = "META-INF/plugin-components";
    
    public static final String COMPONENT_KEY = "component";
    public static final String COMPONENT_INDEX_FILE = INDEX_FILES_DIR + "/" + COMPONENT_KEY;

    public static final String COMPONENT_IMPORT_KEY = "imports";
    public static final String COMPONENT_IMPORT_INDEX_FILE = INDEX_FILES_DIR + "/" + COMPONENT_IMPORT_KEY;
}
