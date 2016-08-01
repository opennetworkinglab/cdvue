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

import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.ILookup;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.utils.IListEnumerable;
import com.yworks.yfiles.view.GraphComponent;
import com.yworks.yfiles.view.HighlightIndicatorManager;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Class that contains methods and variables to handle searching for nodes in a given graph.
 *
 * @author Parth Pendurkar
 * @version 1.0
 */
public class SearchHandler {

    private IGraph graph;
    private GraphComponent graphComponent;

    /**
     * Constructor for objects of class SearchHandler.
     *
     * @param graph             the IGraph that this object will search through
     * @param graphComponent    the GraphComponent that this object will use to highlight found items
     */
    public SearchHandler(IGraph graph, GraphComponent graphComponent) {
        this.graph = graph;
        this.graphComponent = graphComponent;
    }

    /**
     * Creates the GUI for the SearchHandler by making a new SearchPanel and embedding it in a JFrame.
     */
    public void createAndShowGUI() {
        JFrame frame = new JFrame("- Search Nodes -");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new SearchPanel(this));
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Finds and highlights all nodes in the graph with a given name.
     *
     * @param nodeName      the node to search for
     */
    public void findNode(String nodeName) {
        HighlightIndicatorManager manager = graphComponent.getHighlightIndicatorManager();
        manager.clearHighlights();

        IListEnumerable nodes = graph.getNodes();
        nodes.forEach(n -> {
            INode node = (INode) n;
            if (node.getTag().equals(nodeName)) {
                Set<ILookup> toBeHighlighted = new HashSet<>();
                //toBeHighlighted = GraphHandler.highlightOutgoingNodes(node, toBeHighlighted, graphComponent);
                graphComponent.setCurrentItem(node);
                toBeHighlighted.stream().forEach(manager::addHighlight);
            }
        });
    }

    /**
     * Prints previous user input in the console.
     *
     * @param text      the user input
     */
    public void printText(String text) {
        System.out.println("Searched for: " + text);
    }
}
