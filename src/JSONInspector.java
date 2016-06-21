import com.sun.tools.javac.util.List;
import com.thoughtworks.qdox.model.JavaAnnotation;
import com.thoughtworks.qdox.model.JavaClass;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;

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
        ArrayList<JavaAnnotation> jas = (ArrayList) jsonObject.get(className + " annotations");

        System.out.println(className + " has " + jas.size() + " compiled annotations recovered from the JSON");

        return "JSON toString completed.";
    }

}
