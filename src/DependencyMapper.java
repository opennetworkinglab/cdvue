/**
 * Created by parthpendurkar on 6/17/16.
 */

import java.util.Scanner;

public class DependencyMapper {

    private void processFile(String path) throws Exception {
        DependencyParser p = new DependencyParser(path);
        try {
            System.out.println("Executing.");
            p.execute();

            System.out.println("Execution complete. JSON's compiled.");
            System.out.println("Testing...");
            p.test();
        }
        catch (Exception e) {
            System.out.println("Execution failed.");
            e.printStackTrace();
        }
    }

    public DependencyMapper() {
        /*
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
        */
    }

    public static void main(String[] args) {
        //SwingUtilities.invokeLater(DependencyMapper::new);
        DependencyMapper m = new DependencyMapper();
        Scanner sc = new Scanner(System.in);
        System.out.println("Input the path to the java file(s):");
        String path = sc.nextLine();
        try {
            m.processFile(path);
        }
        catch (Exception e) {
            System.out.println("Could not process files...");
            e.printStackTrace();
        }
    }
}