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
 * Created by parthpendurkar on 7/8/16.
 */
public class SearchHandler {

    private IGraph graph;
    private GraphComponent graphComponent;

    public SearchHandler(IGraph graph, GraphComponent graphComponent) {
        this.graph = graph;
        this.graphComponent = graphComponent;
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    public void createAndShowGUI() {
        JFrame frame = new JFrame("- Search Nodes -");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new SearchPanel(this));
        frame.pack();
        frame.setVisible(true);
    }

    public void findNode(String nodeName) {
        HighlightIndicatorManager manager = graphComponent.getHighlightIndicatorManager();
        manager.clearHighlights();

        IListEnumerable nodes = graph.getNodes();
        nodes.forEach(n -> {
            INode node = (INode) n;
            if (node.getTag().equals(nodeName)) {
                Set<ILookup> toBeHighlighted = new HashSet<>();
                toBeHighlighted = GraphHandler.highlightOutgoingNodes(node, toBeHighlighted, graphComponent);
                graphComponent.setCurrentItem(node);
                toBeHighlighted.stream().forEach(manager::addHighlight);
            }
        });
    }

    public void printText(String text) {
        System.out.println("Searched for: " + text);
    }
}
