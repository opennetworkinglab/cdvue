/*
 * Copyright 2015-present Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onlab.cdvue;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaAnnotation;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.expression.AnnotationValue;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.onlab.cdvue.DependencyMapper.println;

/**
 * Class that contains methods and variables to parse through the java files in an inputted filepath and fill out an array of JSONObjects for later use.
 *
 * @author Parth Pendurkar
 * @version 1.0
 */
@SuppressWarnings("unchecked")
class DependencyParser {
    private String path;
    private JSONArray jsonObjects;

    /**
     * Constructor for objects of class org.onlab.cdvue.DependencyParser.
     *
     * @param path      the path to process
     */
    DependencyParser(String path) {
        this.path = path;
        jsonObjects = new JSONArray();
    }

    /**
     * Processes each class found in the inputted path.
     *
     * @throws Exception        if files not found
     */
     void execute() throws Exception {
        try {
            JavaProjectBuilder builder = new JavaProjectBuilder(); //QDox
            builder.addSourceTree(new File(path));
            builder.getClasses().forEach(this::processClass);
        }
        catch (Exception e) {
            println("Couldn't find any java files.");
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Processes the given class and compiles a JSONObject to add to the jsonObjects array.
     *
     * @param javaClass     the given JavaClass
     */
    private void processClass(JavaClass javaClass) {
        if (javaClass.isAbstract())
            return;
        String fullyClassifiedName = javaClass.getFullyQualifiedName();
        JSONObject jsonObject = new JSONObject();

        println("");
        println("Processing class: " + fullyClassifiedName + ".");

        //default values to be loaded into the JSONObject
        boolean isComponent = containsClassAnnotation(javaClass, "Component");
        boolean isService = containsClassAnnotation(javaClass, "Service");
        boolean isInterface = javaClass.isInterface();
        List<JavaAnnotation> classAnnotations = javaClass.getAnnotations();
        List<JavaClass> implementedClassesUnparsed = javaClass.getImplementedInterfaces();
        List<String> implementedClasses = new ArrayList<>();

        //populating implementedClasses List
        for (JavaClass ic : implementedClassesUnparsed) {
            implementedClasses.add(ic.getFullyQualifiedName());
        }

        List<JavaField> fields = javaClass.getFields();
        List<String> referenceFields = new ArrayList<>();
        String serviceTag = "";

        if (!classAnnotations.isEmpty()) {
            //modifying serviceTag if necessary
            serviceTag = getServiceTag(javaClass);

            if (isComponent) {
                referenceFields = processSuperFields(javaClass.getSuperJavaClass(), referenceFields);
            }

            if (isService) {
                implementedClasses = processSuperImplements(javaClass.getSuperJavaClass(), implementedClasses);
            }

            if (isComponent || isService) {
                println("The class has " + classAnnotations.size() + " annotations, and one of them is either Component or Service.");
                List<String> lines = new ArrayList<>();
                println("The class has: " + fields.size() + " fields.");

                //processes each field of the JavaClass
                for (JavaField field : fields)
                    processField(lines, referenceFields, field);
            }
            else
                println("The class has " + classAnnotations.size() + " annotations, but none of them are Component nor Service.");
        }
        else
            println("This class has no annotations.");

        //compiles the JSONObject
        jsonObject.put("cn", fullyClassifiedName); //ease of access to get class's fully classified name
        jsonObject.put(fullyClassifiedName, javaClass); //mapping the fully classified name of the class to the actual JavaClass
        jsonObject.put("hc", isComponent); //whether the class has an @Component annotation
        jsonObject.put("hs", isService); //whether the class has an @Service annotation
        jsonObject.put("ca", classAnnotations); //all JavaAnnotations for the class
        jsonObject.put("ic", implementedClasses); //all JavaClass's that the class implements
        jsonObject.put("rf", referenceFields); //all JavaField's from this class that contain an @Reference annotation
        jsonObject.put("ii", isInterface); //whether or not the JavaClass is an interface or not
        jsonObject.put("st", serviceTag); //if there is a tag for the @Service annotation, this String will not be empty
        jsonObjects.add(jsonObject);
    }

    /**
     * Processes the given JavaField of a specific JavaClass.
     *
     * @param lines                 list of all @Reference annotations on this field
     * @param referenceFields       list of all JavaFields with an @Reference annotation
     * @param field                 the field to be processed.
     */
    private void processField(List<String> lines, List<String> referenceFields, JavaField field) {
        println("");
        println("Processing field.");

        //gets all annotations of the given field
        List<JavaAnnotation> annotations = field.getAnnotations();
        println("The field " + field.getType().getName() + " has " + annotations.size() + " annotations.");

        //filters out all annotations that aren't @Reference. Adds all @Reference annotations to both the given String and JavaField Lists.
        annotations.stream().filter(ja -> (ja.getType().getName().equals("org.apache.felix.scr.annotations.Reference") || ja.getType().getName().equals("Reference"))).forEach(ja -> {
            lines.add(ja.getType().getName());
            referenceFields.add(field.getType().getFullyQualifiedName());
        });
    }

    private List<String> processSuperFields(JavaClass superClass, List<String> referenceFields) {
        if (superClass != null && containsClassAnnotation(superClass, "Component")) {
            for (JavaField javaField : superClass.getFields()) {
                if (javaField.getAnnotations().stream().filter(ja -> (ja.getType().getName().equals("org.apache.felix.scr.annotations.Reference") || ja.getType().getName().equals("Reference"))).count() > 0) {
                    referenceFields.add(javaField.getType().getFullyQualifiedName());
                }
            }
            referenceFields = processSuperFields(superClass.getSuperJavaClass(), referenceFields);
        }
        return referenceFields;
    }

    private List<String> processSuperImplements(JavaClass superClass, List<String> implementedInterfaces) {
        if (superClass != null && containsClassAnnotation(superClass, "Service")) {
            for (JavaClass javaInterface : superClass.getImplementedInterfaces()) {
                implementedInterfaces.add(javaInterface.getFullyQualifiedName());
                String serviceTag = getServiceTag(superClass);
                if (!serviceTag.equals(""))
                    implementedInterfaces.add(getServiceTag(superClass));
                implementedInterfaces = processSuperImplements(superClass.getSuperJavaClass(), implementedInterfaces);
            }
        }
        return implementedInterfaces;
    }

    private String getServiceTag(JavaClass javaClass) {
        String serviceTag = "";
        for (JavaAnnotation ja : javaClass.getAnnotations()) {
            AnnotationValue sT = ja.getProperty("value");
            if (sT != null) {
                serviceTag = ja.getProperty("value").toString();
                serviceTag = serviceTag.substring(0, serviceTag.length() - 6);
                if (serviceTag.equals(javaClass.getFullyQualifiedName()) || serviceTag.equals(javaClass.getName())) {
                    serviceTag = ""; //this takes care of the case where the class is a @Service of itself
                }
            }
        }
        return serviceTag;
    }

    private boolean containsClassAnnotation(JavaClass javaClass, String annotationType) {
        for (JavaAnnotation ja : javaClass.getAnnotations()) {
            String aName = ja.getType().getName();
            if (aName.equals("org.apache.felix.scr.annotations." + annotationType) || aName.equals(annotationType))
                return true;
        }
        return false;
    }

    /**
     * Method that generates a Dependency Mapper graph html file with the compiled JSONObjects.
     */
    void makeGraph() throws IOException {
        GraphHandler g = new GraphHandler(jsonObjects);
        g.prepareGraph();
    }
}