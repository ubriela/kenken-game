/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kenken;

import java.awt.Point;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import kenken.Kenken.Cage;
import kenken.Kenken.Operator;

/**
 *
 * @author ubriela
 */
public class Kenken {

    private static class MyPointComparable implements Comparator<Point> {

        boolean[][] isAssigned;
        public Hashtable<Point, HashSet<Integer>> variableCandidates;

        public MyPointComparable(boolean[][] assigned, Hashtable<Point, HashSet<Integer>> variableCandidates) {
            this.isAssigned = assigned;
            this.variableCandidates = variableCandidates;
        }

        @Override
        public int compare(Point t, Point t1) {
            return (getCandidatesNumber(t) > getCandidatesNumber(t1) ? 1 : 0);
        }

        private int getCandidatesNumber(Point t) {
            return variableCandidates.get(t).size();
        }
    }
    private String filename = "";
//    private boolean isComplete = false;
    private int N;                              //  N-square
    private int assignment[][];                //  a result
    public static boolean isAssigned[][];             //  keep track assigned var
    private Vector<int[][]> resultSets;         //  all results
//    private Vector<Point> originalMatrix;      //  originalMatrix
    public static Hashtable<Point, HashSet<Integer>> variableCandidates;
    private Hashtable<Point, Cage> hashCages;  //  keep track couple -> cage
    private Vector<Cage> cagesList;   //  current cages
//    private Hashtable<Point, Integer> inferences;
    public HashMap<Point, Integer> hashMap;
    public long searchspace = 0;

    public enum Operator {

        equal, plus, minus, multiply, divide
    };

    public static class Cage { //implements Comparable<Cage> 

        Operator opt;
        int number;
        Vector<Point> varList;

        public Cage() {
            varList = new Vector<Point>();
        }

        //  the smaller cage is a cage which has less number of unassigned variables
        public int compareTo(boolean[][] isAssigned, Cage t) {
            return (this.getUnassignedVarNumber(isAssigned, this) > this.getUnassignedVarNumber(isAssigned, t)) ? 1 : 0;
        }

        public int getUnassignedVarNumber(boolean[][] isAssigned, Cage cage) {
            int n = 0;
            for (int i = 0; i < cage.varList.size(); i++) {
                if (!isAssigned[cage.varList.get(i).x][cage.varList.get(i).y]) {
                    n++;
                }
            }
            return n;
        }
    }

    public class MyCageComparable implements Comparator<Cage> {

        boolean[][] isAssigned;

        public MyCageComparable(boolean[][] isAssigned) {
            this.isAssigned = isAssigned;
        }

        @Override
        public int compare(Cage t, Cage t1) {
            return (getUnassignedVarNumber(isAssigned, t) > this.getUnassignedVarNumber(isAssigned, t1)) ? 1 : 0;
        }

        public int getUnassignedVarNumber(boolean[][] isAssigned, Cage cage) {
            int n = 0;
            for (int i = 0; i < cage.varList.size(); i++) {
                if (!isAssigned[cage.varList.get(i).x][cage.varList.get(i).y]) {
                    n++;
                }
            }
            return n;
        }
    }

    public Kenken(String filename) {
        this.filename = filename;
        FileReader textReader = new FileReader(filename);
        N = textReader.getN();
//        originalMatrix = new Vector<Point>();
//        originalMatrix = textReader.getOriginalMatrix();

        cagesList = new Vector<Cage>();
        cagesList = textReader.getCages();

        hashCages = new Hashtable<Point, Cage>();
        hashCages = textReader.getHashCages();

        resultSets = new Vector<int[][]>();
        initialize();
    }

    private void initialize() {
        FileReader.createFile();

        //  we have N & cagesList
        assignment = new int[N][N];        //  default value is 0
        isAssigned = new boolean[N][N];    //  default value is false
        variableCandidates = new Hashtable<Point, HashSet<Integer>>();

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                HashSet<Integer> candidates = new HashSet<Integer>();
                for (int k = 1; k <= N; k++) {
                    candidates.add(Integer.valueOf(k));
                }
                variableCandidates.put(new Point(i, j), candidates);

            }
        }

        fixSingleSquareCage();
    }

    private void fixSingleSquareCage() {
        Iterator it = cagesList.iterator();
        while (it.hasNext()) {
            Cage cage = (Cage) it.next();
            if (cage.opt == Operator.equal) {
                Point couple = cage.varList.firstElement();
                assignment[couple.x][couple.y] = cage.number;
                isAssigned[couple.x][couple.y] = true;
                variableCandidates.get(couple).clear();

                updateVariableCandidates(isAssigned, variableCandidates, couple, cage.number);
            }
        }
    }

    public Hashtable<Point, HashSet<Integer>> copyVariableCandidates(Hashtable<Point, HashSet<Integer>> variableCandidates) {
        Hashtable<Point, HashSet<Integer>> _variableCandidates = new Hashtable<Point, HashSet<Integer>>();

        Enumeration<Point> keys = variableCandidates.keys();
        while (keys.hasMoreElements()) {
            Point point = (Point) keys.nextElement();
            HashSet tmp_hs = variableCandidates.get(point);

            HashSet<Integer> set = new HashSet<Integer>();
            Iterator it = tmp_hs.iterator();
            while (it.hasNext()) {
                int i = (Integer) it.next();
                set.add(Integer.valueOf(i));
            }


            _variableCandidates.put(point, set);
        }

        return _variableCandidates;
    }

    //  BACKTRACKING-SEARCH(csp) return a solution or failure
    public void backTrackingSearch() {
        //  return BACKTRACKING({}, csp)
        boolean backTracking = backTracking(isAssigned, variableCandidates);
        FileReader.writeToFile("Search space (all possible boards): " + String.valueOf(searchspace));
    }

    //  BACKTRACKING(assignment, csp) returns a solution, or failure
    private boolean backTracking(boolean[][] isAssigned, Hashtable<Point, HashSet<Integer>> variableCandidates) {

//        Hashtable<Point, HashSet<Integer>> _variableCandidates = (Hashtable<Point, HashSet<Integer>>)
//                                                            variableCandidates.clone();//copyVariableCandidates(variableCandidates);


        //  if assignment is complete then return assignment
        if (isAssignmentCompleted(isAssigned)) {
            if (isAResult()) {
                //  Save a result
         
                resultSets.add(assignment);
                printResult(assignment);
                //initialize();
                return true;
            }
        }

        //  var = SELECT-UNASSIGNED-VARIABLE(csp)
        //  find the first cage which has at least an unassigned cell
        Point couple = selectUnassignedVariable(isAssigned, variableCandidates);   //  get the smallest cage
        if (couple == null) {
            return false;
        }
        HashSet<Integer> _candidates = variableCandidates.get(couple);
        if (_candidates.isEmpty()) {
            return false;
        }





//        HashSet<Integer> candidates = new HashSet<Integer>();
//        Iterator it = _candidates.iterator();
//        while (it.hasNext()) {
//            int i = (Integer) it.next();
//            candidates.add(Integer.valueOf(i));
//        }


        //  for each value in ORDER-DOMAIN-VALUES(var, assignment, csp) do
        Iterator it2 = _candidates.iterator();
        while (it2.hasNext()) {
            int value = (Integer) it2.next();
            Hashtable<Point, HashSet<Integer>> _variableCandidates = new Hashtable<Point, HashSet<Integer>>();
            _variableCandidates = copyVariableCandidates(variableCandidates);
            boolean[][] newIsAssigned = new boolean[N][N];
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    newIsAssigned[i][j] = isAssigned[i][j];
                }
            }

            //  if value is consistent with assignment then
            if (isConsistentWithConstraints(newIsAssigned, couple, value)) {
                //  add {var = value} to assignment
                assignment[couple.x][couple.y] = value;
                newIsAssigned[couple.x][couple.y] = true;
//                if (couple.x == 3 && couple.y == 0) {
//                    System.out.printf("abc\n", null);
//                }


                //  update variable candidates
                boolean check = updateVariableCandidates(newIsAssigned, _variableCandidates, couple, value);

                if (check == true) {
                    //  result <-- BACKTRACK(assignment, csp)
                    boolean result = backTracking(newIsAssigned, _variableCandidates);
                }

            }

            assignment[couple.x][couple.y] = 0; //  should
            //newIsAssigned[couple.x][couple.y] = false; 

            //  remove {var = value} and inferences from assignment

        }

        //  return failure
        return false;
    }

    //  assignment is completed when all the cells are filled
    private boolean isAssignmentCompleted(boolean[][] isAssigned) {
        this.searchspace++;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (!isAssigned[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isAResult() {
        boolean result = true;
        //  check row
        for (int i = 0; i < N; i++) {
            HashSet<Integer> set = new HashSet<Integer>();
            for (int j = 0; j < N; j++) {
                if (!set.add((Integer) assignment[i][j])) //  false if contains
                {
                    return false;
                }
            }
        }

        //  check column
        for (int j = 0; j < N; j++) {
            HashSet<Integer> set = new HashSet<Integer>();
            for (int i = 0; i < N; i++) {
                if (!set.add((Integer) assignment[i][j])) //  false if contains
                {
                    return false;
                }
            }
        }

        //  check cage
        Iterator it = cagesList.iterator();
        while (it.hasNext()) {
            Cage cage = (Cage) it.next();

            switch (cage.opt) {
                case equal:
                    Iterator itin = cage.varList.iterator();
                    while (itin.hasNext()) {
                        Point couple = (Point) itin.next();
                        if (assignment[couple.x][couple.y] != cage.number) {
                            return false;
                        }
                    }
                    break;
                case plus:
                    Iterator itinPlus = cage.varList.iterator();
                    int sum = 0;
                    while (itinPlus.hasNext()) {
                        Point couple = (Point) itinPlus.next();
                        sum += assignment[couple.x][couple.y];
                    }
                    if (sum != cage.number) {
                        return false;
                    }
                    break;
                case minus:
                    Point couple1 = cage.varList.get(0);
                    Point couple2 = cage.varList.get(1);
                    if (Math.abs(assignment[couple1.x][couple1.y] - assignment[couple2.x][couple2.y]) != cage.number) {
                        return false;
                    }
                    break;
                case multiply:
                    Iterator itinMultiply = cage.varList.iterator();
                    int multiplication = 1;
                    while (itinMultiply.hasNext()) {
                        Point couple = (Point) itinMultiply.next();
                        multiplication *= assignment[couple.x][couple.y];
                    }
                    if (multiplication != cage.number) {
                        return false;
                    }
                    break;
                case divide:
                    Point coupleA = cage.varList.get(0);
                    Point coupleB = cage.varList.get(1);
                    if (assignment[coupleA.x][coupleA.y] > assignment[coupleB.x][coupleB.y]) {
                        if ((int) (Math.abs(assignment[coupleA.x][coupleA.y] / assignment[coupleB.x][coupleB.y])) != cage.number) {
                            return false;
                        }
                    } else {
                        if ((int) (Math.abs(assignment[coupleB.x][coupleB.y] / assignment[coupleA.x][coupleA.y])) != cage.number) {
                            return false;
                        }
                    }
                    break;
                default:
                    return false;
            }
        }

        return result;
    }

    private void printResult(int[][] assignment) {
        for (int j = 0; j < N; j++) {
            for (int i = 0; i < N; i++) {
                FileReader.writeToFile(assignment[i][j] + "\t");
            }
            FileReader.writeToFile("\n");
        }
        
        
//        FileReader.writeToFile("\n");
        FileReader.writeToFile("\n");
    }

    private Point selectUnassignedVariable(boolean[][] isAssigned, Hashtable<Point, HashSet<Integer>> _variableCandidates) {
        Cage cage;
        Point couple = null;
        Collections.sort(cagesList, new MyCageComparable(isAssigned));

        Iterator it = cagesList.iterator();
        while (it.hasNext()) {
            cage = (Cage) it.next();
            Collections.sort(cage.varList, new MyPointComparable(isAssigned, _variableCandidates));
            Iterator it2 = cage.varList.iterator();
            while (it2.hasNext()) {
                couple = (Point) it2.next();
                if (!isAssigned[couple.x][couple.y]) {
                    return couple;
                }
            }

            //  if this cage is fully assigned -> update _variableCandidates, isAssigned
        }

        return null;
    }

    private Point selectUnassignedVariable1(boolean[][] isAssigned, Hashtable<Point, HashSet<Integer>> _variableCandidates) {
        Cage cage;
        Point couple = null;
        Collections.sort(cagesList, new MyCageComparable(isAssigned));

        Iterator it = cagesList.iterator();
        while (it.hasNext()) {
            cage = (Cage) it.next();
            Iterator it2 = cage.varList.iterator();
            while (it2.hasNext()) {
                couple = (Point) it2.next();
                if (!isAssigned[couple.x][couple.y]) {
                    return couple;
                }
            }

            //  if this cage is fully assigned -> update _variableCandidates, isAssigned
        }

        return null;
    }

    private Point selectUnassignedVariable2(boolean[][] isAssigned, Hashtable<Point, HashSet<Integer>> _variableCandidates) {
        Cage cage;
        Point couple = null;

        Iterator it = cagesList.iterator();
        while (it.hasNext()) {
            cage = (Cage) it.next();
            Iterator it2 = cage.varList.iterator();
            while (it2.hasNext()) {
                couple = (Point) it2.next();
                if (!isAssigned[couple.x][couple.y]) {
                    return couple;
                }
            }

            //  if this cage is fully assigned -> update _variableCandidates, isAssigned
        }

        return null;
    }

    /**
     * 
     * @param couple
     * @param value
     * @return false -> backtrack
     */
    private boolean isConsistentWithConstraints(boolean[][] isAssigned, Point couple, int value) {
        boolean consistent = true;
        int[][] _assignment = assignment.clone();
        _assignment[couple.x][couple.y] = value;

        if (!isConsistentWithRowConstraint(_assignment, couple, value)) {
            return false;
        }
        if (!isConsistentWithColumnConstraint(_assignment, couple, value)) {
            return false;
        }
        if (!isConsistentWithCageConstraint(isAssigned, _assignment, couple, value)) {
            return false;
        }
        return consistent;
    }

    private boolean isConsistentWithRowConstraint(int[][] assignment, Point couple, int value) {
        boolean consistent = true;
        for (int i = 0; i < N; i++) {
            if (i != couple.x && assignment[i][couple.y] == value) {
                return false;
            }
        }
        return consistent;
    }

    private boolean isConsistentWithColumnConstraint(int[][] assignment, Point couple, int value) {
        boolean consistent = true;
        for (int j = 0; j < N; j++) {
            if (j != couple.y && assignment[couple.x][j] == value) {
                return false;
            }
        }
        return consistent;
    }

    //  simple cage constraint
    //  if the cage has at least one value leave -> true
    //  else check operator
    private boolean isConsistentWithCageConstraint(boolean[][] isAssigned, int[][] assignment, Point couple, int value) {
        boolean consistent = false;
        Cage cage = hashCages.get(couple);

//        if (cage.varList.size() == 1) {
//            return true;
//        }

        Iterator it = cage.varList.iterator();
        while (it.hasNext()) {
            Point tmp = (Point) it.next();
            if (!isAssigned[tmp.x][tmp.y] && (couple.x != tmp.x || couple.y != tmp.y)) {
                return true;
            }
        }

        //  equal, plus, minus, multiply, devide
        switch (cage.opt) {
            case equal:
                return true;
            case plus:
                Iterator it2 = cage.varList.iterator();
                int sum = 0;
                while (it2.hasNext()) {
                    Point tmp1 = (Point) it2.next();
                    if (couple.x != tmp1.x || couple.y != tmp1.y) {
                        sum += assignment[tmp1.x][tmp1.y];
                    }
                }
                sum += value;
                if (sum == cage.number) {
                    return true;
                }
            case minus:
                Iterator it3 = cage.varList.iterator();
                int subtract = 0;
                while (it3.hasNext()) {
                    Point tmp1 = (Point) it3.next();
                    if (couple.x != tmp1.x || couple.y != tmp1.y) {
                        subtract = Math.abs(value - assignment[tmp1.x][tmp1.y]);
                    }
                }
                if (subtract == cage.number) {
                    return true;
                }
            case multiply:
                Iterator it4 = cage.varList.iterator();
                int multiplication = 1;
                while (it4.hasNext()) {
                    Point tmp1 = (Point) it4.next();
                    if (couple.x != tmp1.x || couple.y != tmp1.y) {
                        multiplication *= assignment[tmp1.x][tmp1.y];
                    }
                }
                multiplication *= value;
                if (multiplication == cage.number) {
                    return true;
                }
            case divide:
                Iterator it5 = cage.varList.iterator();
                double divident = 0;
                while (it5.hasNext()) {
                    Point tmp1 = (Point) it5.next();
                    if (couple.x != tmp1.x || couple.y != tmp1.y) {
                        if (value > assignment[tmp1.x][tmp1.y]) {
                            if (value % assignment[tmp1.x][tmp1.y] == 0) {
                                divident = value / assignment[tmp1.x][tmp1.y];
                            }
                        } else if (assignment[tmp1.x][tmp1.y] % value == 0) {
                            divident = assignment[tmp1.x][tmp1.y] / value;
                        } else {
                            return false;
                        }

                    }
                }
                if (divident == (double) cage.number) {
                    return true;
                }
            default:
                break;
        }

        return consistent;
    }

    private boolean updateVariableCandidates(boolean[][] isAssigned, Hashtable<Point, HashSet<Integer>> _variableCandidates, Point couple, int value) {
        boolean check = updateVariableCandiateInRow(isAssigned, _variableCandidates, couple, value);
        if (check == false) {
            return check;
        }
        check = updateVariableCandidateInColumn(isAssigned, _variableCandidates, couple, value);
        if (check == false) {
            return check;
        }
        check = updateVariableCandidateInCage(isAssigned, _variableCandidates, couple, value);
        if (check == false) {
            return check;
        }
        return check;
    }

    private boolean updateVariableCandiateInRow(boolean[][] isAssigned, Hashtable<Point, HashSet<Integer>> _variableCandidates, Point couple, int value) {
        for (int i = 0; i < N; i++) {
            if (i != couple.x && !isAssigned[i][couple.y]) {
                Point temp = new Point(i, couple.y);
                //Point originalCouple = findOriginalCouple(i, couple.y);
                _variableCandidates.get(temp).remove(value);
                if (_variableCandidates.get(temp).isEmpty()) {
                    return false;
                }
//                HashSet<Integer> candidates = _variableCandidates.get(temp);
//                removeCandidate(candidates, value);
//
//                //  update variable candidate
//                _variableCandidates.put(temp, candidates);

                //  set inferences
//                if (candidates.size() == 1) {
//                    inferences.put(originalCouple, (Integer) candidates.toArray()[0]);
//                    assignment[originalCouple.x][originalCouple.y] = (Integer) candidates.toArray()[0];
//                    isAssigned[originalCouple.x][originalCouple.y] = true;
//                    candidates.clear();
//                    _variableCandidates.put(originalCouple, candidates);
//                }
            }
        }
        return true;
    }

//    private Point findOriginalCouple(int x, int y) {
//        Point tmp = null;
//        Iterator it = originalMatrix.iterator();
//        while (it.hasNext()) {
//            tmp = (Point) it.next();
//            if (tmp.x == x && tmp.y == y) {
//                return tmp;
//            }
//        }
//        return tmp;
//    }
//    private boolean removeCandidate(HashSet<Integer> candidates, int value) {
//        Iterator it = candidates.iterator();
//        while (it.hasNext()) {
//            Integer i = (Integer) it.next();
//            if (i.intValue() == value) {
//                candidates.remove(i);
//                return true;
//            }
//        }
//        return false;
//    }
    private boolean updateVariableCandidateInColumn(boolean[][] isAssigned, Hashtable<Point, HashSet<Integer>> _variableCandidates, Point couple, int value) {
        for (int j = 0; j < N; j++) {
            if (j != couple.y && !isAssigned[couple.x][j]) {
//                Point originalCouple = findOriginalCouple(couple.x, j);
                Point temp = new Point(couple.x, j);
                _variableCandidates.get(temp).remove(value);
                if (_variableCandidates.get(temp).isEmpty()) {
                    return false;
                }
//                HashSet<Integer> candidates = _variableCandidates.get(temp);
//                removeCandidate(candidates, value);
//
//                //  update variable candidate
//                _variableCandidates.put(temp, candidates);     //  replace old value by new value

                //  set inferences
//                if (candidates.size() == 1) {
//                    inferences.put(originalCouple, (Integer) candidates.toArray()[0]);
//                    assignment[originalCouple.x][originalCouple.y] = (Integer) candidates.toArray()[0];
//                    isAssigned[originalCouple.x][originalCouple.y] = true;
//                    candidates.clear();
//                    _variableCandidates.put(originalCouple, candidates);
//                }
            }
        }
        return true;
    }

    /**
     * 
     * @param couple
     * @param value
     * @return false -> backtrack
     */
    private boolean updateVariableCandidateInCage(boolean[][] isAssigned, Hashtable<Point, HashSet<Integer>> _variableCandidates, Point couple, int value) {
        Cage cage = hashCages.get(couple);
//        if (cage.varList.size() == 1) {
//            return;
//        }


        int unassignedNumber = unassignedCoupleNumber(isAssigned, cage);
        if (unassignedNumber == 1) {
            //  equal, plus, minus, multiply, devide
            switch (cage.opt) {
                case equal:
                    return true;
                case plus:
                    Iterator it2 = cage.varList.iterator();
                    int sum = 0;

                    Point originalCouplePlus = null;
                    while (it2.hasNext()) {
                        Point tmp = (Point) it2.next();
                        if (!isAssigned[tmp.x][tmp.y]) {
                            originalCouplePlus = new Point(tmp.x, tmp.y);
                        } else {
                            sum += assignment[tmp.x][tmp.y];
                        }
                    }


                    HashSet<Integer> candidates = _variableCandidates.get(originalCouplePlus);
                    int tmp_plus = cage.number - sum;
                    if (tmp_plus >= 1 && tmp_plus <= N) {

                        if (candidates.contains(cage.number - sum)) {
                            {
                                candidates.clear();
                                candidates.add(cage.number - sum);
                            }
                        } else {
                            _variableCandidates.get(originalCouplePlus).clear();
                            candidates.clear();
                            return false;
                        }

                    } else {
                        _variableCandidates.get(originalCouplePlus).clear();
                        candidates.clear();
                        candidates.clear();
                        return false;   //  backtrack
                    }

                    //  update variable candidate
                    _variableCandidates.put(originalCouplePlus, candidates);     //  replace old value by new value

                    //  set inferences
//                    if (candidates.size() == 1) {
//                        inferences.put(originalCouple, (Integer) candidates.toArray()[0]);
//                        assignment[originalCouple.x][originalCouple.y] = (Integer) candidates.toArray()[0];
//                        isAssigned[originalCouple.x][originalCouple.y] = true;
//                        candidates.clear();
//                        _variableCandidates.put(originalCouple, candidates);
//                    }
                    return true;
                case minus:
                    Iterator it3 = cage.varList.iterator();
                    int sumMinus = 0;

                    Point originalCoupleMinus = null;
                    while (it3.hasNext()) {
                        Point tmp = (Point) it3.next();
                        if (!isAssigned[tmp.x][tmp.y]) {
                            originalCoupleMinus = new Point(tmp.x, tmp.y);
                        } else {
                            sumMinus += assignment[tmp.x][tmp.y];
                        }
                    }
                    //HashSet<Integer> candidatesMinus = _variableCandidates.get(originalCoupleMinus);
                    
                    

                    HashSet<Integer> candidatesMinus = new HashSet<Integer>();
                    int tmp_minus = sumMinus - cage.number;

                    if (tmp_minus > 0) {
                        
                            if (_variableCandidates.get(originalCoupleMinus).contains(tmp_minus)) {
                                //candidatesDevide.clear();
                                candidatesMinus.add(tmp_minus);
                            }
                        }
                    tmp_minus = sumMinus + cage.number;
                    if (tmp_minus <= N) {
                        
                            if (_variableCandidates.get(originalCoupleMinus).contains(tmp_minus)) {
                                //candidatesDevide.clear();
                                candidatesMinus.add(tmp_minus);
                            }
                        }
                    
                    
                    //  update variable candidate
                    _variableCandidates.put(originalCoupleMinus, candidatesMinus);
                    if(candidatesMinus.isEmpty())
                        return false;//  replace old value by new value

                    //  set inferences
//                    if (candidatesMinus.size() == 1) {
//                        inferences.put(originalCoupleMinus, (Integer) candidatesMinus.toArray()[0]);
//                        assignment[originalCoupleMinus.x][originalCoupleMinus.y] = (Integer) candidatesMinus.toArray()[0];
//                        isAssigned[originalCoupleMinus.x][originalCoupleMinus.y] = true;
//                        candidatesMinus.clear();
//                        _variableCandidates.put(originalCoupleMinus, candidatesMinus);
//                    }
                    return true;
                case multiply:
                    Iterator it4 = cage.varList.iterator();
                    int remain = cage.number;
                    Point originalCoupleMultiply = null;
                    boolean check = true;
                    while (it4.hasNext()) {
                        Point tmp = (Point) it4.next();
                        if (!isAssigned[tmp.x][tmp.y]) {
                            originalCoupleMultiply = new Point(tmp.x, tmp.y);
                        } else {
                            if (remain % assignment[tmp.x][tmp.y] == 0) {
                                remain /= assignment[tmp.x][tmp.y];
                            } else {
                                check = false;

                            }
                        }
                    }

                    if (check == false) {
                        _variableCandidates.get(originalCoupleMultiply).clear();
                        return false;
                    }

                    HashSet<Integer> candidatesMultiply = new HashSet<Integer>();

                    if (remain >= 1 && remain <= N) {
                        if (_variableCandidates.get(originalCoupleMultiply).contains(remain)) {
                            candidatesMultiply.add(remain);
                        }
                    }

                    //  update variable candidate
                    _variableCandidates.put(originalCoupleMultiply, candidatesMultiply);
                    if (candidatesMultiply.isEmpty()) {
                        return false;
                    }
                    //  replace old value by new value

                    //  set inferences
//                    if (candidatesMultiply.size() == 1) {
//                        inferences.put(originalCoupleMultiply, (Integer) candidatesMultiply.toArray()[0]);
//                        assignment[originalCoupleMultiply.x][originalCoupleMultiply.y] = (Integer) candidatesMultiply.toArray()[0];
//                        isAssigned[originalCoupleMultiply.x][originalCoupleMultiply.y] = true;
//                        candidatesMultiply.clear();
//                        _variableCandidates.put(originalCoupleMultiply, candidatesMultiply);
//                    }
                    return true;
                case divide:
                    Iterator it5 = cage.varList.iterator();
                    int remainDevide = 0;

                    Point originalCoupleDevide = null;
                    while (it5.hasNext()) {
                        Point tmp = (Point) it5.next();
                        if (!isAssigned[tmp.x][tmp.y]) {
                            originalCoupleDevide = new Point(tmp.x, tmp.y);
                        } else {
                            remainDevide = assignment[tmp.x][tmp.y];
                        }
                    }

                    HashSet<Integer> candidatesDevide = new HashSet<Integer>();


                    if (remainDevide % cage.number == 0) {
                        int tmp_device = remainDevide / cage.number;
                        if (tmp_device >= 1 && tmp_device <= N) {
                            if (_variableCandidates.get(originalCoupleDevide).contains(tmp_device)) {
                                //candidatesDevide.clear();
                                candidatesDevide.add(tmp_device);
                            }
                        }
                    }
                    int tmp_device = cage.number * remainDevide;
                    if (tmp_device >= 1 && tmp_device <= N) {
                        if (_variableCandidates.get(originalCoupleDevide).contains(tmp_device)) {
                            //candidatesDevide.clear();
                            candidatesDevide.add(tmp_device);
                        }
                    }


                    //  update variable candidate

                    _variableCandidates.put(originalCoupleDevide, candidatesDevide);
                    if (candidatesDevide.isEmpty()) {
                        return false;
                    }
                    //  replace old value by new value

                    //  set inferences
//                    if (candidatesDevide.size() == 1) {
//                        inferences.put(originalCoupleDevide, (Integer) candidatesDevide.toArray()[0]);
//                        assignment[originalCoupleDevide.x][originalCoupleDevide.y] = (Integer) candidatesDevide.toArray()[0];
//                        isAssigned[originalCoupleDevide.x][originalCoupleDevide.y] = true;
//                        candidatesDevide.clear();
//                        _variableCandidates.put(originalCoupleDevide, candidatesDevide);
//                    }
                    return true;




                default:
                    break;
            }
        }
        return true;
    }

    private int unassignedCoupleNumber(boolean[][] isAssigned, Cage cage) {
        int n = 0;
        Iterator it = cage.varList.iterator();
        while (it.hasNext()) {
            Point couple = (Point) it.next();
            if (!isAssigned[couple.x][couple.y]) {
                n++;
            }
        }
        return n;
    }

    //  if inferences <> failure then
//    //  add inferences to assignment
//    private void updateInferences(Hashtable<Point, HashSet<Integer>> _variableCandidates) {
//        Enumeration<Point> keys = inferences.keys();
//        while (keys.hasMoreElements()) {
//            Point couple = keys.nextElement();
//            int value = inferences.get(couple);
//            if (isConsistentWithConstraints(couple, value)) {
//                addInferenceToAssignment(_variableCandidates, couple, value);
//            }
//        }
//    }
//
//    private void addInferenceToAssignment(Hashtable<Point, HashSet<Integer>> _variableCandidates, Point couple, int value) {
//        assignment[couple.x][couple.y] = value;
//        isAssigned[couple.x][couple.y] = true;
//        inferences.remove(couple);
//
//        //  update variable candidates
//        updateVariableCandidates(_variableCandidates, couple, value);
//    }
//    private void removeInferencesFromAssignment() {
//        Enumeration<Point> keys = inferences.keys();
//        while (keys.hasMoreElements()) {
//            Point couple = keys.nextElement();
//            int value = inferences.get(couple);
//            removeInferenceFromAssignment(couple, value);
//        }
//    }
//    private void removeInferenceFromAssignment(Point couple, int value) {
//        assignment[couple.x][couple.y] = 0;
//        isAssigned[couple.x][couple.y] = false;
//    }
    private void addCandidateToAssignment() {
    }

    private void removeCandidateFromAssignment() {
    }
}