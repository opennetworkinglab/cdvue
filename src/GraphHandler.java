import com.thoughtworks.qdox.model.JavaAnnotation;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;

/**
 * Created by parthpendurkar on 6/23/16.
 */
public class GraphHandler
{
    private JSONArray jsonObjects;
    private Map<String, Set<String>> serviceToComponents;
    private Map<String, Set<String>> componentToReferences;

    public GraphHandler(JSONArray jsonObjects) {
        this.jsonObjects = jsonObjects;
        serviceToComponents = new HashMap<>();
        componentToReferences = new HashMap<>();
    }

    public void prepareData() {
        JSONInspector jsonInspector = new JSONInspector();

        for (int i = 0; i < jsonObjects.size(); i++)
        {
            JSONObject j = (JSONObject) jsonObjects.get(i);
            jsonInspector.toString(j);
            populateServiceMap(j);
            populateComponentMap(j);
        }

        System.out.println(serviceToComponents.size());
        System.out.println(componentToReferences.size());
    }

    private void populateServiceMap(JSONObject j) {
        String className = (String) j.get("class name");
        if ((boolean) j.get(className + ":hs")) {
            List<JavaClass> classes = (List) j.get(className + ":ic");
            //List<JavaAnnotation> classAnnotations = (List) j.get(className + ":ca");

            for (JavaClass ic : classes) {
                String fullICName = ic.getFullyQualifiedName();
                Set<String> currentForIC = serviceToComponents.get(fullICName);
                if (currentForIC == null)
                    currentForIC = new HashSet<>();
                currentForIC.add(className);
                serviceToComponents.put(fullICName, currentForIC);
            }
            // TODO: Check if the @Service tag itself has attributes here and add class names accordingly.
            // TODO: Take care of services that aren't implemented by any classes (eg. InterfaceC should point to an empty set
        }
    }

    private void populateComponentMap(JSONObject j) {
        String className = (String) j.get("class name");
        if ((boolean) j.get(className + ":hc")) {
            List <JavaField> fields = (List) j.get(className + ":f");
            for (JavaField f : fields) {
                Set<String> currentForC = componentToReferences.get(className);
                if (currentForC == null)
                    currentForC = new HashSet<>();
                currentForC.add(f.getType().getFullyQualifiedName());
                componentToReferences.put(className, currentForC);
            }
        }
    }
}
