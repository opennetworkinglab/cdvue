import com.thoughtworks.qdox.model.JavaAnnotation;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaParameterizedType;
import com.thoughtworks.qdox.model.impl.DefaultJavaParameterizedType;
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
        //JavaClass c = (JavaClass) jsonObject.get(className);
        boolean hSA = (boolean) jsonObject.get(className + " has service");
        boolean hCA = (boolean) jsonObject.get(className + " has component");
        List<JavaAnnotation> jca = (LinkedList) jsonObject.get(className + " class annotations");
        List<JavaAnnotation> jac = (ArrayList) jsonObject.get(className + " annotations");
        List<JavaClass> jic = (List) jsonObject.get(className + " implemented classes");

        System.out.println("JSON toString for " + className + ".");
        System.out.println("");

        System.out.println(className + " has service annotations: " + hSA);
        System.out.println(className + " has component annotations: " + hCA);
        System.out.println(className + " has " + jca.size() + " class annotations recovered from the JSON");
        System.out.println(className + " has " + jac.size() + " compiled annotations recovered from the JSON");

        if (jic.size() > 0)
        {
            System.out.println(className + " implements all of the classes below");
            jic.forEach(jc -> System.out.println("-" + jc.getName()));
        }

        System.out.println("");
        return "JSON toString for " + className + " completed.";
    }

}
