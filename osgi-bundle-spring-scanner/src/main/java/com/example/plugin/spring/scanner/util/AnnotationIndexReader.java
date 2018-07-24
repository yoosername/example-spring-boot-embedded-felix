package com.example.plugin.spring.scanner.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.Bundle;

import com.google.common.base.Charsets;

/**
 * A utility class to read index files from a classloader or bundle.
 * Can handle reading a single index file, or concating multiple index files based on the
 * currently running product.
 * 
 */
public class AnnotationIndexReader
{
    /**
     * reads a single file from a bundle
     * @param resourceFile
     * @param bundle
     * @return
     */
    public static List<String> readIndexFile(String resourceFile, Bundle bundle)
    {
        URL url = bundle.getResource(resourceFile);
        return readIndexFile(url);
    }

    /**
     * reads a single file from a classloader
     * @param resourceFile
     * @param classLoader
     * @return
     */
    public static List<String> readIndexFile(String resourceFile, ClassLoader classLoader)
    {
        URL url = classLoader.getResource(resourceFile);
        return readIndexFile(url);
    }

    /**
     * reads the cross-product file as well as the product-specific file for the currently running product
     * and returns their contents as a single list
     * 
     * @param resourceFile
     * @param bundle
     * @return
     */
    public static List<String> readAllIndexFilesForProduct(String resourceFile, Bundle bundle)
    {
        List<String> entries = new ArrayList<String>();
        
        URL url = bundle.getResource(resourceFile);
        
        entries.addAll(readIndexFile(url));
        
        return entries;
    }

    /**
     * reads the cross-product file as well as the product-specific file for the currently running product
     * and returns their contents as a single list
     * 
     * @param resourceFile
     * @param classLoader
     * @return
     */
    public static List<String> readAllIndexFilesForProduct(String resourceFile, ClassLoader classLoader)
    {
        List<String> entries = new ArrayList<String>();

        URL url = classLoader.getResource(resourceFile);

        entries.addAll(readIndexFile(url));

        return entries;
    }

    public static List<String> readIndexFile(URL url)
    {
        List<String> resources = new ArrayList<String>();

        try
        {
            if (null == url)
            {
                return resources;
            }

            BufferedReader reader;
            try
            {
                reader = new BufferedReader(new InputStreamReader(url.openStream(), Charsets.UTF_8));
            }
            catch (FileNotFoundException e)
            {
                return resources;
            }

            String line = reader.readLine();
            while (line != null)
            {
                resources.add(line);

                line = reader.readLine();
            }

            reader.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Cannot read index file [" + url.toString() + "]", e);
        }

        return resources;
    }
}
