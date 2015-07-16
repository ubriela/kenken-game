/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kenken;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import kenken.Kenken.Cage;
import kenken.Kenken.Operator;

/**
 *
 * @author ubriela
 */
public class FileReader {

    static String filename = "";
    int N = 0;
    private Vector<Cage> cagesList = new Vector<Cage>();
//    private Vector<Point> originalMatrix = new Vector<Point>();
    private Hashtable<Point, Cage> hashCages = new Hashtable<Point, Cage>();

    public FileReader(String filename) {
        this.filename = filename;
        try {
            FileInputStream fstream = new FileInputStream(filename);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;

            N = Integer.parseInt(br.readLine());

            //Read File Line By Line
            while ((strLine = br.readLine()) != null) {
                String[] arrStr = strLine.split(";");
                Cage cage = new Cage();
                String total = arrStr[0];

                cage.number = Integer.valueOf(total.split(",")[0]);

                if (total.split(",")[1].equalsIgnoreCase("=")) {
                    cage.opt = Operator.equal;
                } else if (total.split(",")[1].equalsIgnoreCase("+")) {
                    cage.opt = Operator.plus;
                } else if (total.split(",")[1].equalsIgnoreCase("-")) {
                    cage.opt = Operator.minus;
                } else if (total.split(",")[1].equalsIgnoreCase("*")) {
                    cage.opt = Operator.multiply;
                } else if (total.split(",")[1].equalsIgnoreCase("/")) {
                    cage.opt = Operator.divide;
                }

                for (int i = 1; i < arrStr.length; i++) {
                    total = arrStr[i];
                    Point couple = new Point(Integer.valueOf(total.split(",")[0]), Integer.valueOf(total.split(",")[1]));
//                    originalMatrix.add(couple);
                    cage.varList.add(couple);
                    hashCages.put(couple, cage);
                }

                cagesList.add(cage);
            }
            //Close the input stream
            in.close();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public int getN() {

        return N;
    }

    public Vector<Cage> getCages() {
        return cagesList;
    }

//    public Vector<Point> getOriginalMatrix() {
//        return originalMatrix;
//    }
    Hashtable<Point, Cage> getHashCages() {
        return hashCages;
    }

    public static void writeToFile(String line) {
        try {
            FileWriter fstream = new FileWriter(filename + ".out", true);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(line);
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(FileReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void createFile() {
        try {
            FileWriter fstream = new FileWriter(filename + ".out");
            BufferedWriter out = new BufferedWriter(fstream);
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(FileReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
