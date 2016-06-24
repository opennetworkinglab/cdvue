/**
 * Created by parthpendurkar on 6/20/16.
 */

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class DependencyParser
{
    /*
    private static final String COMPONENT = "org.apache.felix.scr.annotations.Component";
    private static final String SERVICE = "org.apache.felix.scr.annotations.Service";
    private static final String REFERENCE = "org.apache.felix.scr.annotations.Reference";
    private static final String PROPERTY = "org.apache.felix.scr.annotations.Property";
    private static final String SEP = "|";
    */

    private String path;
    private JSONArray jsonObjects;

    public DependencyParser(String path)
    {
        this.path = path;
        jsonObjects = new JSONArray();
    }

    public void execute() throws Exception
    {
        try
        {
            JavaProjectBuilder builder = new JavaProjectBuilder();
            builder.addSourceTree(new File(path));
            builder.getClasses().forEach(this::processClass);
        }
        catch (Exception e)
        {
            System.out.println("Couldn't find any classes");
            e.printStackTrace();
            throw e;
        }
    }

    private void processClass(JavaClass javaClass)
    {
        System.out.println("");
        System.out.println("Processing class: " + javaClass.getFullyQualifiedName());

        List<JavaAnnotation> classAnnotations = javaClass.getAnnotations();
        boolean isComponent = false;
        boolean isService = false;

        if (!classAnnotations.isEmpty())
        {
            for (JavaAnnotation ja : classAnnotations)
            {
                String aName = ja.getType().getName();
                if (aName.equals("Component"))
                {
                    isComponent = true;
                }
                else if (aName.equals("Service"))
                {
                    isService = true;
                }
            }
            if (!(isComponent || isService))
            {
                System.out.println("The class has " + classAnnotations.size() + " annotations, but none of them are Component nor Service.");
            }
        }
        else
        {
            System.out.println("This class has no annotations.");
        }

        if (isComponent || isService) {
            System.out.println("The class has " + classAnnotations.size() + " annotations, and one of them is either Component or Service.");

            List<String> lines = new ArrayList<>();
            List<JavaField> fieldsWithRefAnns = new ArrayList<>();
            List<JavaClass> implementedClasses = javaClass.getImplementedInterfaces();
            List<JavaField> fields = javaClass.getFields();

            System.out.println("The class has: " + fields.size() + " fields.");

            fields.forEach(field -> processField(lines, fieldsWithRefAnns, javaClass, field));

            JSONObject jsonObject = new JSONObject();
            String fullyClassifiedName = javaClass.getFullyQualifiedName();

            jsonObject.put("class name", fullyClassifiedName);
            jsonObject.put(fullyClassifiedName, javaClass);
            jsonObject.put(fullyClassifiedName + ":hc", isComponent);
            jsonObject.put(fullyClassifiedName + ":hs", isService);
            jsonObject.put(fullyClassifiedName + ":ca", classAnnotations);
            jsonObject.put(fullyClassifiedName + ":ic", implementedClasses);
            jsonObject.put(fullyClassifiedName + ":f", fieldsWithRefAnns);

            if (!lines.isEmpty())
            {
                writeCatalog(javaClass, lines);
            }

            jsonObjects.add(jsonObject);
            //testJSON(javaClass.getFullyQualifiedName());

        }
    }

    private void processField(List<String> lines, List<JavaField> jas, JavaClass javaClass, JavaField field)
    {
        System.out.println("");
        System.out.println("Processing field.");

        List<JavaAnnotation> annotations = field.getAnnotations();

        System.out.println("The field " + field.getType().getName() + " has " + annotations.size() + " annotations.");
        annotations.forEach(ja -> {
            if (ja.getType().getName().equals("Reference"))
            {
                lines.add(ja.getType().getName());
                jas.add(field);
            }
        });
    }

    private void writeCatalog(JavaClass javaClass, List<String> lines) {
        System.out.println("");
        System.out.println("");
        System.out.println("");
        System.out.println("Writing catalog");

        File dir = new File(path);
        dir.mkdirs();

        System.out.println("Catalog to be saved in " + dir.getAbsolutePath());

        File catalog = new File(dir, javaClass.getName().replace('.', '/') + ".txt");
        try (PrintWriter pw = new PrintWriter(new FileWriter(catalog)))
        {
            pw.println("# This file is auto-generated by Dependency Parser");
            lines.forEach(pw::println);
            System.out.println("Catalog for " + javaClass.getName() + " written successfully.");
        } catch (IOException e) {
            System.err.println("Unable to write catalog for " + javaClass.getName());
            e.printStackTrace();
        }
        System.out.println("");
    }

    private void testJSON(String className)
    {
        JSONInspector j = new JSONInspector((JSONObject) jsonObjects.get(0));
        System.out.println(j.toString(className));
    }
}
