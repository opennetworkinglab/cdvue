import java.util.Scanner;

/**
 * Created by parthpendurkar on 6/17/16.
 */
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

    public static void main(String[] args) {
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