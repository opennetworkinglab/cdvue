import com.thoughtworks.qdox.model.JavaAnnotation;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import org.json.simple.JSONObject;

import java.util.List;

/**
 * Created by parthpendurkar on 6/21/16.
 */
public class JSONInspector {
    private JSONObject jsonObject;

    public JSONInspector(JSONObject jsonObject)
    {
        this.jsonObject = jsonObject;
    }

    public String toString(String className) {
        boolean hCA = (boolean) jsonObject.get(className + ":hc"); //has component
        boolean hSA = (boolean) jsonObject.get(className + ":hs"); //has service
        boolean iI = (boolean) jsonObject.get(className + ":ii"); //is interface
        List<JavaAnnotation> jca = (List) jsonObject.get(className + ":ca"); //class annotations
        List<JavaField> jfa = (List) jsonObject.get(className + ":f"); //annotations
        List<JavaClass> jic = (List) jsonObject.get(className + ":ic"); //implemented classes

        System.out.println("JSON toString for " + className + ".");
        System.out.println("");

        System.out.println(className + " has service annotations: " + hSA);
        System.out.println(className + " has component annotations: " + hCA);
        System.out.println(className + " is an interface: " + iI);

        if (!jca.isEmpty())
            System.out.println(className + " has " + jca.size() + " class annotations recovered from the JSON.");
        if (!jfa.isEmpty())
            System.out.println(className + " has " + jfa.size() + " fields with reference annotations recovered from the JSON.");
        if (!jic.isEmpty()) {
            System.out.println(className + " implements all of the classes below");
            jic.forEach(jc -> System.out.println("-" + jc.getName()));
        }

        System.out.println("");
        return "JSON toString for " + className + " completed.";
    }

}
