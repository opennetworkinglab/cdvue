import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.yworks.yfiles.algorithms.Graph;
import com.yworks.yfiles.geometry.RectD;
import com.yworks.yfiles.graph.*;
import com.yworks.yfiles.graph.labelmodels.InteriorLabelModel;
import com.yworks.yfiles.graph.styles.IArrow;
import com.yworks.yfiles.graph.styles.PolylineEdgeStyle;
import com.yworks.yfiles.graph.styles.ShinyPlateNodeStyle;
import com.yworks.yfiles.layout.circular.CircularLayout;
import com.yworks.yfiles.utils.IList;
import com.yworks.yfiles.utils.IListEnumerable;
import com.yworks.yfiles.view.GraphComponent;
import com.yworks.yfiles.view.HighlightIndicatorManager;
import com.yworks.yfiles.view.input.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.List;

/**
 * Created by parthpendurkar on 6/23/16.
 */
public class GraphHandler
{
    private JSONArray jsonObjects;
    private GraphComponent graphComponent;
    private Map<String, Set<String>> serviceToComponents;
    private Map<String, Set<String>> componentToReferences;
    private Map<String, Boolean> isolatedNodes;
    private ShinyPlateNodeStyle linkedNodeStyle;
    private ShinyPlateNodeStyle isolatedNodeStyle;
    private ShinyPlateNodeStyle interfaceNodeStyle;
    private PolylineEdgeStyle defaultEdgeStyle;
    private static final RectD NODE_SHELL = new RectD(10, 10, 500, 50);


    public GraphHandler(JSONArray jsonObjects) {
        this.jsonObjects = jsonObjects;
        graphComponent = new GraphComponent();
        serviceToComponents = new HashMap<>();
        componentToReferences = new HashMap<>();
        isolatedNodes = new HashMap<>();

        linkedNodeStyle = new ShinyPlateNodeStyle();
        isolatedNodeStyle = new ShinyPlateNodeStyle();
        interfaceNodeStyle = new ShinyPlateNodeStyle();
        defaultEdgeStyle = new PolylineEdgeStyle();

        linkedNodeStyle.setPaint(Color.GREEN);
        linkedNodeStyle.setShadowDrawingEnabled(true);
        isolatedNodeStyle.setPaint(Color.CYAN);
        isolatedNodeStyle.setShadowDrawingEnabled(true);
        interfaceNodeStyle.setPaint(Color.ORANGE);
        interfaceNodeStyle.setShadowDrawingEnabled(true);
        defaultEdgeStyle.setTargetArrow(IArrow.DEFAULT);
    }

    private void prepareData() {
        JSONInspector jsonInspector = new JSONInspector();

        for (int i = 0; i < jsonObjects.size(); i++)
        {
            JSONObject j = (JSONObject) jsonObjects.get(i);
            jsonInspector.toString(j);
            populateMaps(j);
        }
    }

    private void populateMaps(JSONObject j) {
        String className = (String) j.get("class name");
        boolean hc = (boolean) j.get(className + ":hc");
        boolean hs = (boolean) j.get(className + ":hs");
        boolean ii = (boolean) j.get(className + ":ii");

        if (hc) {
            List <JavaField> fields = (List) j.get(className + ":f");
            for (JavaField f : fields) {
                Set<String> currentForC = componentToReferences.get(className);
                if (currentForC == null)
                    currentForC = new HashSet<>();
                currentForC.add(f.getType().getFullyQualifiedName());
                componentToReferences.put(className, currentForC);
            }
        }
        if (hs) {
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
        }
        if (!(hc || hs)) {
            isolatedNodes.put(className, ii);
        }
    }

    public void prepareGraph() {
        prepareData();
        JFrame frame = new JFrame("- Dependency Mapper -");
        GraphViewerInputMode graphViewerInputMode = new GraphViewerInputMode();
        graphViewerInputMode.setClickableItems(GraphItemTypes.NODE);
        graphViewerInputMode.getItemHoverInputMode().setEnabled(true);
        graphViewerInputMode.getItemHoverInputMode().setHoverItems(GraphItemTypes.EDGE.or(GraphItemTypes.NODE));
        graphViewerInputMode.getItemHoverInputMode().setInvalidItemsDiscardingEnabled(false);
        //graphViewerInputMode.getItemHoverInputMode().addHoveredItemChangedListener(this::onHoveredItemChanged);
        graphViewerInputMode.addItemClickedListener(this::onItemClicked);
        graphViewerInputMode.getClickInputMode().addClickedListener(this::onNoItemClicked);

        graphComponent.getGraph().getNodeDefaults().setStyle(isolatedNodeStyle);
        frame.setSize(800, 800);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.add(graphComponent, BorderLayout.CENTER);

        IGraph graph = graphComponent.getGraph();
        setupIsolatedNodes(graph);
        buildComponentNodes(graph);
        finishServiceNodeConnections(graph);
        LayoutUtilities.applyLayout(graph, new CircularLayout());

        graphComponent.setInputMode(graphViewerInputMode);
    }

    private void onHoveredItemChanged(Object sender, HoveredItemChangedEventArgs hoveredItemChangedEventArgs) {
        if (hoveredItemChangedEventArgs.getItem() != null) {
            HighlightIndicatorManager manager = graphComponent.getHighlightIndicatorManager();
            manager.clearHighlights();

            IModelItem newItem = hoveredItemChangedEventArgs.getItem();
            if (newItem != null)
            {
                if (newItem instanceof INode)
                {
                    Set<ILookup> toBeHighlighted = new HashSet<>();
                    toBeHighlighted = highlightOutgoingNodes(newItem, toBeHighlighted);
                    for (ILookup item : toBeHighlighted)
                        manager.addHighlight(item);
                } else if (newItem instanceof IEdge)
                {
                    IEdge edge = (IEdge) newItem;
                    manager.addHighlight(newItem);
                    manager.addHighlight(edge.getSourceNode());
                    manager.addHighlight(edge.getTargetNode());
                }
            }
        }
    }

    private void onItemClicked(Object sender, ItemClickedEventArgs e) {
        HighlightIndicatorManager manager = graphComponent.getHighlightIndicatorManager();
        manager.clearHighlights();

        if (!e.isHandled() && e.getItem() instanceof INode) {
            INode node = (INode) e.getItem();
            this.graphComponent.setCurrentItem(node);
            Set<ILookup> toBeHighlighted = new HashSet<>();
            toBeHighlighted = highlightOutgoingNodes(node, toBeHighlighted);
            for (ILookup item : toBeHighlighted) {
                manager.addHighlight(item);
            }
        }
    }

    private void onNoItemClicked(Object sender, ClickEventArgs c) {
        if (this.graphComponent.getGraphModelManager().hitElementsAt(c.getLocation()).stream().count() == 0) {
            HighlightIndicatorManager manager = graphComponent.getHighlightIndicatorManager();
            manager.clearHighlights();
            c.setHandled(true);
        }
    }

    private Set highlightOutgoingNodes(IModelItem item, Set<ILookup> oldToBeHighlighted) {
        Set<ILookup> toBeHighlighted = new HashSet<>();
        INode node = (INode) item;
        if (!oldToBeHighlighted.contains(node))
            toBeHighlighted.add(node);
        IListEnumerable edges = graphComponent.getGraph().outEdgesAt(node);
        ArrayList<ILookup> parsedEdges = new ArrayList<>();
        for (Object e : edges) {
            if (!oldToBeHighlighted.contains(e))
                parsedEdges.add((IEdge) e);
        }

        for (Object e : parsedEdges) {
            toBeHighlighted.add((IEdge) e);
            toBeHighlighted.addAll(oldToBeHighlighted);
            toBeHighlighted.addAll(highlightOutgoingNodes(((IEdge) e).getTargetNode(), toBeHighlighted));
        }

        return toBeHighlighted;
    }

    private void setupIsolatedNodes(IGraph graph) {
        if (!isolatedNodes.isEmpty()) {
            for (Map.Entry<String, Boolean> entry : isolatedNodes.entrySet()) {
                String className = entry.getKey();
                boolean ii = entry.getValue();
                if (ii)
                    graph.createNode(NODE_SHELL, interfaceNodeStyle, className);
                else
                    graph.createNode(NODE_SHELL, isolatedNodeStyle, className);
                INode node = getNodeWithName(className, graph);
                graph.addLabel(node, className, InteriorLabelModel.CENTER);
            }
        }
    }

    private void buildComponentNodes(IGraph graph) {
        if (!componentToReferences.isEmpty()) {
            for (Map.Entry<String, Set<String>> entry : componentToReferences.entrySet()) {
                String componentClassName = entry.getKey();
                graph.createNode(NODE_SHELL, linkedNodeStyle, componentClassName);
                INode componentNode = getNodeWithName(componentClassName, graph);
                graph.addLabel(componentNode, componentClassName, InteriorLabelModel.CENTER);
                linkNode(componentNode, entry.getValue(), graph);
            }
        }
    }

    private void finishServiceNodeConnections(IGraph graph) {
        if (!serviceToComponents.isEmpty()) {
            for (Map.Entry<String, Set<String>> entry : serviceToComponents.entrySet()) {
                String interfaceName = entry.getKey();
                if (!nodePresentWithName(interfaceName, graph))
                    graph.createNode(NODE_SHELL, linkedNodeStyle, interfaceName);
                INode interfaceNode = getNodeWithName(interfaceName, graph);
                graph.addLabel(interfaceNode, interfaceName, InteriorLabelModel.CENTER);
                linkNode(interfaceNode, entry.getValue(), graph);
            }
        }
    }

    private void linkNode(INode node, Set<String> names, IGraph graph) {
        for (String name : names) {
            if (!nodePresentWithName(name, graph)) {
                graph.createNode(NODE_SHELL, linkedNodeStyle, name);
            }
            INode nodeToLink = getNodeWithName(name, graph);
            if (nodeToLink != null) {
                graph.addLabel(nodeToLink, name, InteriorLabelModel.CENTER);
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
