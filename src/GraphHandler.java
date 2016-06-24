import com.thoughtworks.qdox.model.JavaAnnotation;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.onlab.graph.*;

import java.util.*;

/**
 * Created by parthpendurkar on 6/23/16.
 */
public class GraphHandler
{
    private JSONArray jsonObjects;
    private Map<String, Set<String>> serviceToComponents;
    private Map<String, Set<String>> componentToReferences;

    public GraphHandler(JSONArray jsonObjects)
    {
        this.jsonObjects = jsonObjects;
        serviceToComponents = new HashMap<>();
        componentToReferences = new HashMap<>();
    }

    public void populateServiceMap()
    {
        for (int i = 0; i < jsonObjects.size(); i++)
        {
            JSONObject j = (JSONObject) jsonObjects.get(i);
            String className = (String) j.get("class name");
            if ((boolean) j.get(className + ":hs"))
            {
                ((List) j.get(className + ":ic")).forEach(ic -> {
                    String fullICName = ((JavaClass) ic).getFullyQualifiedName();
                    Set<String> currentForIC = serviceToComponents.get(fullICName);
                    if (currentForIC == null)
                    {
                        currentForIC = new HashSet<>();
                    }
                        currentForIC.add(className);
                        serviceToComponents.put(fullICName, currentForIC);
                });

                // TODO: Check if the @Service tag itself has attributes here and add class names accordingly.

            }
        }
    }

    public void populateComponentMap()
    {
        for (int i = 0; i < jsonObjects.size(); i++)
        {
            JSONObject j = (JSONObject) jsonObjects.get(i);
            String className = (String) j.get("class name");
            if ((boolean) j.get(className + ":hc"))
            {
                ((List) j.get(className + ":f")).forEach(a -> {
                    JavaField f = (JavaField) a;
                    Set<String> currentForC = componentToReferences.get(className);
                    if (currentForC == null)
                    {
                        currentForC = new HashSet<>();
                    }
                    currentForC.add(f.getType().getFullyQualifiedName());
                    componentToReferences.put(className, currentForC);
                });
            }
        }
    }

}
