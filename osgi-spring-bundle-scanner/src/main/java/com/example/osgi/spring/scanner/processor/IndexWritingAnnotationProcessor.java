package com.example.osgi.spring.scanner.processor;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import org.apache.commons.lang.StringUtils;

import com.example.osgi.spring.scanner.util.ClassIndexFiles;

/**
 * The base class for all processors that need to write class index files
 */
public abstract class IndexWritingAnnotationProcessor extends AbstractProcessor implements Processor
{
    private Map<String, Set<String>> annotatedTypeMap = new HashMap<String, Set<String>>();
    
    @Override
    public SourceVersion getSupportedSourceVersion()
    {
        return SourceVersion.latest();

    }
    
    protected void doProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv, String indexKey)
    {
        try
        {
            for (TypeElement anno : annotations)
            {
                for (Element element : roundEnv.getElementsAnnotatedWith(anno))
                {
                    //we delegate to the subclasses for this as they may need special handling based on the kind of element
                    TypeAndAnnotation typeAndAnnotation = getTypeAndAnnotation(element, anno);
                    if (null == typeAndAnnotation || null == typeAndAnnotation.getTypeElement())
                    {
                        continue;
                    }

                    //get the lower-case name of the product from the annotation
                    String lowerFilterName = indexKey;

                    //get the bean name specififed on the annotation (as it's value) if any
                    String nameFromAnnotation = "";
                    try
                    {
                        Annotation componentAnno = typeAndAnnotation.getAnnotation();
                        Method valueMethod = componentAnno.getClass().getDeclaredMethod("value");
                        nameFromAnnotation = (String) valueMethod.invoke(componentAnno);
                    }
                    catch (NoSuchMethodException e)
                    {
                        //ignore
                    }
                    catch (InvocationTargetException e)
                    {
                        //ignore
                    }
                    catch (IllegalAccessException e)
                    {
                        //ignore
                    }

                    //Build the string to put in the index file. Always starts with fully qualified classname
                    StringBuilder sb = new StringBuilder(typeAndAnnotation.getTypeElement().getQualifiedName().toString());

                    //if we have a custom bean name, tack it on
                    if (StringUtils.isNotBlank(nameFromAnnotation))
                    {
                        sb.append("#").append(nameFromAnnotation);
                    }

                    String typeName = sb.toString();
                    
                    //get the proper list to add our type to based on product name (or cross-product)
                    Set<String> typeNames = getAnnotatedTypeNames(lowerFilterName);

                    if (!typeNames.contains(typeName))
                    {
                        typeNames.add(typeName);
                    }
                }
            }

            if (!roundEnv.processingOver())
            {
                return;
            }

            writeIndexFiles(indexKey);

        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the fully qualified class name of the thing that's annotated as well as the instance of the annotation.
     * This delegates to subclasses as they may need to have special handling of this based on the kind of element we're dealing with.
     * @param element
     * @param anno
     * @return
     */
    public abstract TypeAndAnnotation getTypeAndAnnotation(Element element, TypeElement anno);

    /**
     * Reads an existing index file (if any) and adds it's entries to the passed in set
     */
    protected void readOldIndexFile(Set<String> entries, String resourceName, Filer filer) throws IOException
    {
        BufferedReader reader = null;
        try
        {
            FileObject resource = filer.getResource(StandardLocation.CLASS_OUTPUT, "", resourceName);
            Reader resourceReader = resource.openReader(true);
            reader = new BufferedReader(resourceReader);

            String line = reader.readLine();
            while (line != null)
            {
                entries.add(line);
                line = reader.readLine();
            }
        }
        catch (FileNotFoundException e)
        {
        }
        catch (IOException e)
        {
            // Thrown by Eclipse JDT when not found
        }
        catch (UnsupportedOperationException e)
        {
            // Java6 does not support reading old index files
        }
        finally
        {
            if (reader != null)
            {
                reader.close();
            }
        }
    }

    /**
     * returns the map of productName -> classNameSet
     * @return
     */
    public Map<String, Set<String>> getAnnotatedTypeMap()
    {
        return annotatedTypeMap;
    }

    /**
     * gets a single set of classNames for the given product name
     * @param lowerFilterName
     * @return
     */
    public Set<String> getAnnotatedTypeNames(String lowerFilterName)
    {
        if (!getAnnotatedTypeMap().containsKey(lowerFilterName))
        {
            getAnnotatedTypeMap().put(lowerFilterName, new HashSet<String>());
        }

        return getAnnotatedTypeMap().get(lowerFilterName);
    }

    /**
     * writes out the index files for the given annotion suffix (e.g. Component or ComponentImport)
     * handles writing out noth cross-product and product-specific files
     * @param file
     * @param suffix
     * @throws IOException
     */
    protected void writeIndexFiles(String indexKey) throws IOException
    {
        for (Map.Entry<String, Set<String>> entry : getAnnotatedTypeMap().entrySet())
        {
            StringBuilder filePath = new StringBuilder(ClassIndexFiles.INDEX_FILES_DIR );
            filePath.append("/").append(indexKey);
            
            if (!entry.getKey().equals(indexKey))
            {
                filePath.append("-").append(entry.getKey());
            }

            writeIndexFile(entry.getValue(), filePath.toString(), processingEnv.getFiler());
        }
    }

    /**
     * writes out a single index file
     * @param elementNameList
     * @param resourceName
     * @param filer
     * @throws IOException
     */
    protected void writeIndexFile(Iterable<String> elementNameList, String resourceName, Filer filer)
            throws IOException
    {
        Set<String> entries = new HashSet<String>();
        for (String elementName : elementNameList)
        {
            entries.add(elementName);
        }

        readOldIndexFile(entries, resourceName, filer);

        FileObject file = filer.createResource(StandardLocation.CLASS_OUTPUT, "", resourceName);
        Writer writer = file.openWriter();
        for (String entry : entries)
        {
            writer.write(entry);
            writer.write("\n");
        }
        writer.close();
    }

    /**
     * used to return the class type and annotation
     */
    protected class TypeAndAnnotation
    {
        private final TypeElement typeElement;
        private final Annotation annotation;

        protected TypeAndAnnotation(TypeElement typeElement, Annotation annotation)
        {
            this.typeElement = typeElement;
            this.annotation = annotation;
        }

        public TypeElement getTypeElement()
        {
            return typeElement;
        }

        public Annotation getAnnotation()
        {
            return annotation;
        }
    }
}
