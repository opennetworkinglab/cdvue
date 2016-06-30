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

import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.yworks.yfiles.geometry.RectD;
import com.yworks.yfiles.graph.*;
import com.yworks.yfiles.graph.labelmodels.InteriorLabelModel;
import com.yworks.yfiles.graph.styles.IArrow;
import com.yworks.yfiles.graph.styles.PolylineEdgeStyle;
import com.yworks.yfiles.graph.styles.ShinyPlateNodeStyle;
import com.yworks.yfiles.layout.circular.CircularLayout;
import com.yworks.yfiles.utils.IListEnumerable;
import com.yworks.yfiles.view.GraphComponent;
import com.yworks.yfiles.view.HighlightIndicatorManager;
import com.yworks.yfiles.view.input.ClickEventArgs;
import com.yworks.yfiles.view.input.GraphViewerInputMode;
import com.yworks.yfiles.view.input.HoveredItemChangedEventArgs;
import com.yworks.yfiles.view.input.ItemClickedEventArgs;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Class that contains methods and variables to generate mappings and generates a graph of the inputted JSONObject's.
 *
 * @author Parth Pendurkar
 * @version 1.0
 */
public class GraphHandler
{
    private JSONArray jsonObjects;
    private GraphComponent graphComponent;
    private Map<String, Set<String>> serviceToComponents; //the map of service interfaces to the set of component classes that implement them
    private Map<String, Set<String>> componentToReferences; //the map of component classes to the set of interfaces they refer to
    private Map<String, Boolean> isolatedNodes; //all isolated nodes (interfaces and classes that are not linked to anything)

    private ShinyPlateNodeStyle linkedNodeStyle;
    private ShinyPlateNodeStyle isolatedNodeStyle;
    private ShinyPlateNodeStyle interfaceNodeStyle;
    private PolylineEdgeStyle defaultEdgeStyle;
    private static final RectD NODE_SHELL = new RectD(10, 10, 500, 50);

    /**
     * Constructor for objects of class GraphHandler.
     *
     * @param jsonObjects       the list of (specifically organized) JSONObject's to generate the graph from
     */
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

    /**
     * Iteratively populates the two maps with each JSONObject. Also inspects them with a JSONInspector object.
     */
    private void prepareData() {
        JSONInspector jsonInspector = new JSONInspector();

        for (int i = 0; i < jsonObjects.size(); i++)
        {
            JSONObject j = (JSONObject) jsonObjects.get(i);
            jsonInspector.toString(j); //prints out useful information to the console
            populateMaps(j);
        }
    }

    /**
     * Populates the two maps with given JSONObject data.
     *
     * @param j     the given JSONObject
     */
    private void populateMaps(JSONObject j) {
        //getting basic information
        String className = (String) j.get("cn");
        boolean hc = (boolean) j.get("hc");
        boolean hs = (boolean) j.get("hs");
        boolean ii = (boolean) j.get("ii");

        //if component class, modify componentToReferences Map
        if (hc) {
            List <JavaField> fields = (List) j.get("rf");
            Set<String> currentForC = new HashSet<>();
            for (JavaField f : fields)
                currentForC.add(f.getType().getFullyQualifiedName()); //adds the fully qualified name of the interface the annotation refers to
            componentToReferences.put(className, currentForC);
        }
        //if service class, modify serviceToComponents Map
        if (hs) {
            List<JavaClass> classes = (List) j.get("ic");
            for (JavaClass ic : classes) {
                String fullICName = ic.getFullyQualifiedName();
                Set<String> currentForIC = serviceToComponents.get(fullICName); //gets previous Set for current implemented interface
                if (currentForIC == null)
                    currentForIC = new HashSet<>(); //creates a new Set if there was no previous Set for current interface
                currentForIC.add(className);
                serviceToComponents.put(fullICName, currentForIC);
            }
            //TODO: Check if the @Service tag itself has attributes here and add class names accordingly.
        }
        if (!(hc || hs)) {
            isolatedNodes.put(className, ii); //puts isolated nodes in the isolatedNodes map, with the fully qualified name mapping to if it's an interface or not
        }
    }

    /**
     * Prepares and displays the yFiles graph.
     */
    public void prepareGraph() {
        //populating maps
        prepareData();
        JFrame frame = new JFrame("- Dependency Mapper -");
        GraphViewerInputMode graphViewerInputMode = new GraphViewerInputMode();
        graphViewerInputMode.setClickableItems(GraphItemTypes.NODE);
        graphViewerInputMode.getItemHoverInputMode().setEnabled(true);
        graphViewerInputMode.getItemHoverInputMode().setHoverItems(GraphItemTypes.EDGE.or(GraphItemTypes.NODE));
        graphViewerInputMode.getItemHoverInputMode().setInvalidItemsDiscardingEnabled(false);

        //initializing the input listeners
        //add if you want hover functionality (somewhat buggy): graphViewerInputMode.getItemHoverInputMode().addHoveredItemChangedListener(this::onHoveredItemChanged);
        graphViewerInputMode.addItemClickedListener(this::onItemClicked);
        graphViewerInputMode.getClickInputMode().addClickedListener(this::onNoItemClicked);

        //setting visual style
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

    /**
     * Called when an item on the graph is clicked. Calls helper method to highlight 'relevant' nodes.
     *
     * @param sender
     * @param e
     */
    private void onItemClicked(Object sender, ItemClickedEventArgs e) {
        HighlightIndicatorManager manager = graphComponent.getHighlightIndicatorManager();
        manager.clearHighlights();

        if (!e.isHandled() && e.getItem() instanceof INode) {
            INode node = (INode) e.getItem();
            Set<ILookup> toBeHighlighted = new HashSet<>();
            toBeHighlighted = highlightOutgoingNodes(node, toBeHighlighted);
            toBeHighlighted.stream().forEach(manager::addHighlight);
        }
    }

    /**
     * Called if mouse is clicked, but not on an item. Clears all highlights.
     *
     * @param sender
     * @param c
     */
    private void onNoItemClicked(Object sender, ClickEventArgs c) {
        if (graphComponent.getGraphModelManager().hitElementsAt(c.getLocation()).stream().count() == 0) {
            HighlightIndicatorManager manager = graphComponent.getHighlightIndicatorManager();
            manager.clearHighlights();
            c.setHandled(true);
        }
    }

    /**
     * Recursive method to return a Set of all 'relevant' nodes. Gets the outbound edges from a node and calls the same method on the
     * nodes that those edges reach, until nodes that have no outbound edges are reached. This essentially shows what is related to
     * the class or interface that is clicked.
     *
     * @param item                      the current node
     * @param oldToBeHighlighted        the old Set of what is to be highlighted
     * @return                          the new Set of what is to be highlighted
     */
    private Set highlightOutgoingNodes(IModelItem item, Set<ILookup> oldToBeHighlighted) {
        Set<ILookup> toBeHighlighted = new HashSet<>();
        INode node = (INode) item;
        if (!oldToBeHighlighted.contains(node))
            toBeHighlighted.add(node); //adds current node if not already present in old Set
        IListEnumerable edges = graphComponent.getGraph().outEdgesAt(node); //gets outbound edges from given node
        ArrayList<ILookup> parsedEdges = new ArrayList<>();
        for (Object e : edges) {
            if (!oldToBeHighlighted.contains(e))
                parsedEdges.add((IEdge) e); //from the list of outbound edges, only gets those that have not been traversed yet
        }

        for (Object e : parsedEdges) {
            toBeHighlighted.add((IEdge) e); //adds current parsed edge
            toBeHighlighted.addAll(oldToBeHighlighted); //combines new Set with old Set
            toBeHighlighted.addAll(highlightOutgoingNodes(((IEdge) e).getTargetNode(), toBeHighlighted)); //recursive call to target node of current outbound edge
        }

        return toBeHighlighted;
    }

    /**
     * Puts elements from isolatedNodes on to the given graph.
     *
     * @param graph     the given IGraph
     */
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

    /**
     * Puts all nodes in the componentToReferences Map on the given graph.
     *
     * @param graph     the given IGraph
     */
    private void buildComponentNodes(IGraph graph) {
        if (!componentToReferences.isEmpty()) {
            //iterates through each entry in the Map
            for (Map.Entry<String, Set<String>> entry : componentToReferences.entrySet()) {
                String componentClassName = entry.getKey();
                graph.createNode(NODE_SHELL, linkedNodeStyle, componentClassName);
                INode componentNode = getNodeWithName(componentClassName, graph);
                graph.addLabel(componentNode, componentClassName, InteriorLabelModel.CENTER);
                linkNode(componentNode, entry.getValue(), graph); //link the node to every interface node in its associated Set
            }
        }
    }

    /**
     * Finishes connecting service interface nodes (if present already) to the component nodes that implement them
     * using the serviceToComponents Map.
     *
     * @param graph     the given IGraph
     */
    private void finishServiceNodeConnections(IGraph graph) {
        if (!serviceToComponents.isEmpty()) {
            for (Map.Entry<String, Set<String>> entry : serviceToComponents.entrySet()) {
                String interfaceName = entry.getKey();
                if (!nodePresentWithName(interfaceName, graph))
                    graph.createNode(NODE_SHELL, linkedNodeStyle, interfaceName); //if the interface node isn't present, create it
                INode interfaceNode = getNodeWithName(interfaceName, graph);
                graph.addLabel(interfaceNode, interfaceName, InteriorLabelModel.CENTER);
                linkNode(interfaceNode, entry.getValue(), graph); //link the node to every component node in its associated Set
            }
        }
    }

    /**
     * Helper method to link a node to everything in its associated Set.
     *
     * @param node      the given INode
     * @param names     the given Set
     * @param graph     the given IGraph
     */
    private void linkNode(INode node, Set<String> names, IGraph graph) {
        for (String name : names) {
            if (!nodePresentWithName(name, graph)) {
                graph.createNode(NODE_SHELL, linkedNodeStyle, name); //if a node with this name isn't present, create it
            }
            INode nodeToLink = getNodeWithName(name, graph); //get the node to link
            if (nodeToLink != null) {
                graph.addLabel(nodeToLink, name, InteriorLabelModel.CENTER);
                graph.createEdge(node, nodeToLink, defaultEdgeStyle); //create the edge between the two nodes
            }
            else
                System.out.println("nodeToLink was null");
        }
    }

    /**
     * Helper method to check if node with a certain name is present in a given graph.
     *
     * @param name      the name to check for
     * @param graph     the given IGraph
     * @return          true if the node is present, else
     *                  false
     */
    private boolean nodePresentWithName(String name, IGraph graph) {
        IListEnumerable currentNodes = graph.getNodes();
        Iterator i = currentNodes.iterator();
        while (i.hasNext()) {
            if (((INode) i.next()).getTag().equals(name))
                return true;
        }
        return false;
    }

    /**
     * Helper method to get node with certain name in a given graph.
     *
     * @param name      the name of the node to get
     * @param graph     the given IGraph
     * @return          the INode with the tag that equals the given name
     */
    private INode getNodeWithName(String name, IGraph graph) {
        IListEnumerable currentNodes = graph.getNodes();
        Iterator i = currentNodes.iterator();
        while (i.hasNext()) {
            INode node = (INode) i.next();
            if (node.getTag().equals(name)) //checks to see if the INode's tag equals the inputted name
                return node;
        }
        return null; //returns null if node not found
    }
}