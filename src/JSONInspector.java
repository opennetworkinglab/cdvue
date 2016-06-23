import com.thoughtworks.qdox.model.JavaAnnotation;
import com.thoughtworks.qdox.model.JavaClass;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by parthpendurkar on 6/21/16.
 */
public class JSONInspector
{
    private JSONObject jsonObject;

    public JSONInspector(JSONObject jsonObject)
    {
        this.jsonObject = jsonObject;
    }

    public String toString(String className)
    {
        JavaClass c = (JavaClass) jsonObject.get(className);
        boolean hSA = (boolean) jsonObject.get(className + " has service");
        boolean hCA = (boolean) jsonObject.get(className + " has component");
        List<JavaAnnotation> jca = (LinkedList) jsonObject.get(className + " class annotations");
        List<JavaAnnotation> jas = (ArrayList) jsonObject.get(className + " annotations");

        System.out.println(className + " has service annotations: " + hSA);
        System.out.println(className + " has component annotations: " + hCA);
        System.out.println(className + " has " + jca.size() + " class annotations recovered from the JSON");
        System.out.println(className + " has " + jas.size() + " compiled annotations recovered from the JSON");

        return "JSON toString for " + className + " completed.";
    }

}
