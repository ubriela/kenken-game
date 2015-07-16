/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kenken;

/**
 *
 * @author ubriela
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Kenken kenken1 = new Kenken("./src/kenken/test1");
        kenken1.backTrackingSearch();

        Kenken kenken2 = new Kenken("./src/kenken/test2");
        kenken2.backTrackingSearch();

        Kenken kenken3 = new Kenken("./src/kenken/test3");
        kenken3.backTrackingSearch();
    }
}
