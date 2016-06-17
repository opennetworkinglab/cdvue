/**
 * Created by parthpendurkar on 6/17/16.
 */
import com.yworks.yfiles.geometry.RectD;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.styles.ShinyPlateNodeStyle;
import com.yworks.yfiles.view.GraphComponent;
import com.yworks.yfiles.view.input.GraphEditorInputMode;

import javax.swing.*;
import java.awt.*;
//package sample;

public class DependencyMapper {

    public DependencyMapper() {
        JFrame frame = new JFrame("Hello! yFiles for Java.");
        GraphComponent graphComponent = new GraphComponent();
        ShinyPlateNodeStyle nodeStyle = new ShinyPlateNodeStyle();
        ShinyPlateNodeStyle defaultNodeStyle = new ShinyPlateNodeStyle();

        nodeStyle.setPaint(Color.ORANGE);
        nodeStyle.setShadowDrawingEnabled(true);
        defaultNodeStyle.setPaint(Color.BLUE);

        graphComponent.getGraph().getNodeDefaults().setStyle(defaultNodeStyle);

        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.add(graphComponent, BorderLayout.CENTER);

        IGraph graph = graphComponent.getGraph();
        graph.createNode(new RectD(10,10,100,100), nodeStyle);
        graph.createNode(new RectD(150,150,100,100), nodeStyle);
        graph.createNode(new RectD(250,250,100,100), nodeStyle);
        graphComponent.setInputMode(new GraphEditorInputMode());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(DependencyMapper::new);
    }
}