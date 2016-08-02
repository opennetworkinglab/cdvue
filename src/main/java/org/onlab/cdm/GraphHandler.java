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
package org.onlab.cdm;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.util.*;

/**
 * Class that contains methods and variables to generate mappings and generates a graph of the inputted JSONObject's.
 *
 * @author Parth Pendurkar
 * @version 1.0
 */
@SuppressWarnings("unchecked")
class GraphHandler
{
    private JSONArray jsonObjects;
    private Map<String, Set<String>> serviceToComponents; //the map of service interfaces to the set of component classes that implement them
    private Map<String, Set<String>> componentToReferences; //the map of component classes to the set of interfaces they refer to
    private Map<String, Set<String>> componentToServices; //the map of component classes to the set of interfaces they implement
    //private Map<String, Boolean> isolatedNodes; //all isolated nodes (interfaces and classes that are not linked to anything)
    private JSONArray catalog;

    private static final String TITLE_PLACEHOLDER = "TITLE_PLACEHOLDER";
    private static final String DATA_PLACEHOLDER = "DATA_PLACEHOLDER";

    /**
     * Constructor for objects of class org.onlab.cdm.GraphHandler.
     *
     * @param jsonObjects       the list of (specifically organized) JSONObject's to generate the graph from
     */
    GraphHandler(JSONArray jsonObjects) {
        this.jsonObjects = jsonObjects;
        serviceToComponents = new HashMap<>();
        componentToReferences = new HashMap<>();
        componentToServices = new HashMap<>();
        //isolatedNodes = new HashMap<>();
        catalog = new JSONArray();
    }

    /**
     * Iteratively populates the two maps with each JSONObject. Also inspects them with a org.onlab.cdm.JSONInspector object.
     */
    private void prepareData() {
        //org.onlab.cdm.JSONInspector jsonInspector = new org.onlab.cdm.JSONInspector(); //uncomment this and the line below to get useful information in the console

        for (Object jsonObject : jsonObjects)
        {
            JSONObject j = (JSONObject) jsonObject;
            //jsonInspector.toString(j); //prints out useful information to the console, uncomment if you want this information in the console
            try
            {
                populateMaps(j);
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * Populates the two maps with given JSONObject data.
     *
     * @param j     the given JSONObject
     */
    private void populateMaps(JSONObject j) throws Exception {
        //getting basic information
        String className = (String) j.get("cn");
        boolean hc = (boolean) j.get("hc");
        boolean hs = (boolean) j.get("hs");
        //boolean ii = (boolean) j.get("ii");

        //if component class, modify componentToReferences Map
        if (hc) {
            List <String> fields = (List) j.get("rf");
            Set<String> currentForC = new HashSet<>(fields); //adds the fully qualified name of the interface the annotation refers to
            componentToReferences.put(className, currentForC);
        }
        //if service and component class, modify serviceToComponents Map
        if (hs && hc) {
            List<String> classes = (List) j.get("ic");

            Set<String> classSet = new HashSet<>(classes);
            componentToServices.put(className, classSet);

            String serviceTag = (String) j.get("st");
            if (!serviceTag.equals("")) {
                Set<String> previousClassSet = componentToServices.get(className);
                previousClassSet.add(serviceTag);
                componentToServices.put(className, previousClassSet);

                Set<String> cForIC = serviceToComponents.get(serviceTag); //gets previous Set for current implemented interface
                if (cForIC == null)
                    cForIC = new HashSet<>(); //creates a new Set if there was no previous Set for current interface
                cForIC.add(className);
                serviceToComponents.put(serviceTag, cForIC);
            }
            for (String icName : classes) {
                Set<String> currentForIC = serviceToComponents.get(icName); //gets previous Set for current implemented interface
                if (currentForIC == null)
                    currentForIC = new HashSet<>(); //creates a new Set if there was no previous Set for current interface
                currentForIC.add(className);
                serviceToComponents.put(icName, currentForIC);
            }
        }
        //if (!(hc || hs))
            //isolatedNodes.put(className, ii); //puts isolated nodes in the isolatedNodes map, with the fully qualified name mapping to if it's an interface or no
    }

    /**
     * Prepares and displays the yFiles graph.
     */
    void prepareGraph() throws IOException
    {
        prepareData();
        buildComponentNodes();

        String index = slurp(getClass().getResourceAsStream("/index.html"));

        FileWriter fw = new FileWriter("mapper.html");
        fw.write(index.replace(TITLE_PLACEHOLDER, "title here").replace(DATA_PLACEHOLDER, catalog.toJSONString()));
        fw.close();
    }

    /**
     * Slurps the specified input stream into a string.
     *
     * @param stream input stream to be read
     * @return string containing the contents of the input stream
     * @throws IOException if issues encountered reading from the stream
     */
    private static String slurp(InputStream stream) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line).append(System.lineSeparator());
        }
        br.close();
        return sb.toString();
    }

    /**
     * Puts all nodes in the componentToReferences Map on the given graph.
     */
    private void buildComponentNodes() {
        if (!componentToReferences.isEmpty()) {
            //iterates through each entry in the Map
            for (Map.Entry<String, Set<String>> entry : componentToReferences.entrySet()) {
                String componentClassName = entry.getKey();

                JSONObject newJSON = getJSONWithNameFromArray(componentClassName, catalog);

                if (newJSON == null)
                    newJSON = addNewJSONToArray(componentClassName, new JSONArray(), 0, new JSONArray(), 0, new JSONArray(), catalog);
                linkNode(componentClassName, entry.getValue(), newJSON); //link the node to every interface node in its associated Set
            }
        }
    }

    /**
     * Helper method to link a node to everything in its associated Set.
     *
     * @param names     the given Set
     */
    private void linkNode(String className, Set<String> names, JSONObject jsonObject) {
        String nameFromJSON = (String) jsonObject.get("name");
        JSONArray JSONarray = (JSONArray) jsonObject.get("dependsOn");
        JSONArray dependsOnServices = (JSONArray) jsonObject.get("dependsOnServices");
        JSONArray dependentsServices = (JSONArray) jsonObject.get("dependentsServices");

        if (names != null)
            dependsOnServices.addAll(names);


        Set<String> dependents = componentToServices.get(className);
        if (dependents != null)
            dependentsServices.addAll(dependents);

        int dependsOn = 0;
        JSONObject jsonObject1;

        if (names != null) {
            for (String name : names) {
                Set<String> retrievedComponents = serviceToComponents.get(name);
                if (retrievedComponents == null) {
                    if (!stringPresentInArray(name + "?", JSONarray)) {
                        if (!JSONPresentInArray(name + "?", catalog)) {
                            JSONObject ghostJSON = new JSONObject();
                            ghostJSON.put("name", name + "?");
                            ghostJSON.put("dependsOn", new JSONArray());
                            ghostJSON.put("numberDependsOn", "N/A");
                            ghostJSON.put("dependsOnServices", new JSONArray());
                            ghostJSON.put("numberDependents", 0);
                            List ghostServices = new ArrayList();
                            ghostServices.add(name);
                            ghostJSON.put("dependentsServices", new ArrayList<>(ghostServices));
                            catalog.add(ghostJSON);
                        }
                        jsonObject1 = getJSONWithNameFromArray(name + "?", catalog);
                        JSONarray.add(name + "?");
                        int previousNumberDependents = (int) jsonObject1.get("numberDependents");
                        jsonObject1.put("numberDependents", previousNumberDependents + 1);
                        dependsOn++;
                    }
                }
                else {
                    for (String n : retrievedComponents) {
                        if (!stringPresentInArray(n, JSONarray) && !n.equals(nameFromJSON)) {
                            jsonObject1 = getJSONWithNameFromArray(n, catalog);
                            if (jsonObject1 == null)
                                jsonObject1 = addNewJSONToArray(n, new JSONArray(), 0, new JSONArray(), 0, new JSONArray(), catalog);
                            JSONarray.add(n);
                            int previousNumberDependents = (int) jsonObject1.get("numberDependents");
                            jsonObject1.put("numberDependents", previousNumberDependents + 1);
                            dependsOn++;
                        }
                    }
                }
            }
        }
        else {
            System.out.println("Names was null");
        }

        jsonObject.put("numberDependsOn", dependsOn);
        jsonObject.put("dependsOn", JSONarray);
        jsonObject.put("dependsOnServices", dependsOnServices);
        jsonObject.put("dependentsServices", dependentsServices);
    }

    private boolean stringPresentInArray(String value, JSONArray jsonArray) {
        for (Object i : jsonArray) {
            if (value.equals(i))
                return true;
        }
        return false;
    }

    private boolean JSONPresentInArray(String name, JSONArray jsonArray) {
        for (Object i : jsonArray) {
            if (name.equals(((JSONObject) i).get("name")))
                return true;
        }
        return false;
    }

    private JSONObject getJSONWithNameFromArray(String name, JSONArray jsonArray) {
        for (Object o : jsonArray) {
            JSONObject jsonObject = (JSONObject) o;
            if (jsonObject.get("name").equals(name))
                return (JSONObject) o;
        }
        return null;
    }

    private JSONObject addNewJSONToArray(String name, JSONArray dependsOn, int numberDependsOn, JSONArray dependsOnServices, int numberDependents, JSONArray dependentsServices, JSONArray jsonArray) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", name);
        jsonObject.put("dependsOn", dependsOn);
        jsonObject.put("numberDependsOn", numberDependsOn);
        jsonObject.put("dependsOnServices", dependsOnServices);
        jsonObject.put("numberDependents", numberDependents);
        jsonObject.put("dependentsServices", dependentsServices);
        jsonArray.add(jsonObject);

        return jsonObject;
    }
}