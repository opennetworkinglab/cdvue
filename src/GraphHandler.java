import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.yworks.yfiles.geometry.RectD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.LayoutUtilities;
import com.yworks.yfiles.graph.labelmodels.ExteriorLabelModel;
import com.yworks.yfiles.graph.styles.IArrow;
import com.yworks.yfiles.graph.styles.PolylineEdgeStyle;
import com.yworks.yfiles.graph.styles.ShinyPlateNodeStyle;
import com.yworks.yfiles.layout.circular.CircularLayout;
import com.yworks.yfiles.utils.IListEnumerable;
import com.yworks.yfiles.view.GraphComponent;
import com.yworks.yfiles.view.input.GraphEditorInputMode;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by parthpendurkar on 6/23/16.
 */
public class GraphHandler
{
    private JSONArray jsonObjects;
    private Map<String, Set<String>> serviceToComponents;
    private Map<String, Set<String>> componentToReferences;
    private ShinyPlateNodeStyle linkedNodeStyle;
    private ShinyPlateNodeStyle isolatedNodeStyle;
    private PolylineEdgeStyle defaultEdgeStyle;
    private static final RectD nodeShell = new RectD(10, 10, 50, 50);


    public GraphHandler(JSONArray jsonObjects) {
        this.jsonObjects = jsonObjects;
        serviceToComponents = new HashMap<>();
        componentToReferences = new HashMap<>();

        linkedNodeStyle = new ShinyPlateNodeStyle();
        isolatedNodeStyle = new ShinyPlateNodeStyle();
        defaultEdgeStyle = new PolylineEdgeStyle();

        linkedNodeStyle.setPaint(Color.ORANGE);
        linkedNodeStyle.setShadowDrawingEnabled(true);
        isolatedNodeStyle.setPaint(Color.BLUE);
        defaultEdgeStyle.setTargetArrow(IArrow.DEFAULT);
    }

    private void prepareData() {
        JSONInspector jsonInspector = new JSONInspector();

        for (int i = 0; i < jsonObjects.size(); i++)
        {
            JSONObject j = (JSONObject) jsonObjects.get(i);
            jsonInspector.toString(j);
            populateServiceMap(j);
            populateComponentMap(j);
        }
    }

    private void populateServiceMap(JSONObject j) {
        String className = (String) j.get("class name");
        if ((boolean) j.get(className + ":hs")) {
            List<JavaClass> classes = (List) j.get(className + ":ic");
            for (JavaClass ic : classes) {
                String fullICName = ic.getFullyQualifiedName();
                Set<String> currentForIC = serviceToComponents.get(fullICName);
                if (currentForIC == null)
                    currentForIC = new HashSet<>();
                currentForIC.add(className);
                serviceToComponents.put(fullICName, currentForIC);
            }
            // TODO: Check if the @Service tag itself has attributes here and add class names accordingly.
            // TODO: Take care of services that aren't implemented by any classes (eg. InterfaceC should point to an empty set)
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

    public void prepareGraph()
    {
        prepareData();
        JFrame frame = new JFrame("- Dependency Mapper -");
        GraphComponent graphComponent = new GraphComponent();

        graphComponent.getGraph().getNodeDefaults().setStyle(isolatedNodeStyle);
        frame.setSize(800, 800);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.add(graphComponent, BorderLayout.CENTER);

        IGraph graph = graphComponent.getGraph();
        buildComponentNodes(graph);
        finishServiceNodeConnections(graph);
        LayoutUtilities.applyLayout(graph, new CircularLayout());

        graphComponent.setInputMode(new GraphEditorInputMode());
    }

    private void buildComponentNodes(IGraph graph) {
        if (!componentToReferences.isEmpty()) {
            for (Map.Entry<String, Set<String>> entry : componentToReferences.entrySet()) {
                String componentClassName = entry.getKey();
                graph.createNode(nodeShell, linkedNodeStyle, componentClassName);
                INode componentNode = getNodeWithName(componentClassName, graph);
                graph.addLabel(componentNode, componentClassName, ExteriorLabelModel.NORTH);
                linkNode(componentNode, entry.getValue(), graph);
            }
        }
    }

    private void finishServiceNodeConnections(IGraph graph) {
        if (!serviceToComponents.isEmpty()) {
            for (Map.Entry<String, Set<String>> entry : serviceToComponents.entrySet()) {
                String interfaceName = entry.getKey();
                if (!nodePresentWithName(interfaceName, graph))
                    graph.createNode(nodeShell, linkedNodeStyle, interfaceName);
                INode interfaceNode = getNodeWithName(interfaceName, graph);
                linkNode(interfaceNode, entry.getValue(), graph);
            }
        }
    }

    private void linkNode(INode node, Set<String> names, IGraph graph) {
        for (String name : names) {
            if (!nodePresentWithName(name, graph)) {
                graph.createNode(nodeShell, linkedNodeStyle, name);
            }
            INode nodeToLink = getNodeWithName(name, graph);
            if (nodeToLink != null) {
                graph.addLabel(nodeToLink, name, ExteriorLabelModel.NORTH);
                graph.createEdge(node, nodeToLink, defaultEdgeStyle);
            }
            else
                System.out.println("nodeToLink was null");
        }
    }

    private boolean nodePresentWithName(String name, IGraph graph) {
        IListEnumerable currentNodes = graph.getNodes();
        Iterator i = currentNodes.iterator();
        while (i.hasNext()) {
            if (((INode) i.next()).getTag().equals(name))
                return true;
        }
        return false;
    }

    private INode getNodeWithName(String name, IGraph graph) {
        IListEnumerable currentNodes = graph.getNodes();
        Iterator i = currentNodes.iterator();
        while (i.hasNext()) {
            INode node = (INode) i.next();
            if (node.getTag().equals(name))
                return node;
        }
        return null;
    }
}
