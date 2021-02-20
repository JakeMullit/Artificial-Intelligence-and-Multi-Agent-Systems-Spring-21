package searchclient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class GraphSearch {

    public static Action[][] search(State initialState, Frontier frontier)
    {
        boolean outputFixedSolution = false;

        if (outputFixedSolution) {
            //Part 1:
            //The agents will perform the sequence of actions returned by this method.
            //Try to solve a few levels by hand, enter the found solutions below, and run them:

            List<Action[]> actions = new ArrayList<Action[]>();
            for(var i = 0; i < 2; i++){
                actions.add(new Action[]{Action.MoveS});
            }
            for(var i = 0; i < 10; i++){
                actions.add(new Action[]{Action.MoveE});
            }
            for(var i = 0; i < 2; i++){
                actions.add(new Action[]{Action.MoveS});
            }
            return actions.toArray(new Action[][]{});
        } else {
            //Part 2:
            //Now try to implement the Graph-Search algorithm from R&N figure 3.7
            //In the case of "failure to find a solution" you should return null.
            //Some useful methods on the state class which you will need to use are:
            //state.isGoalState() - Returns true if the state is a goal state.
            //state.extractPlan() - Returns the Array of actions used to reach this state.
            //state.getExpandedStates() - Returns an ArrayList<State> containing the states reachable from the current state.
            //You should also take a look at Frontier.java to see which methods the Frontier interface exposes
            //
            //printSearchStates(explored, frontier): As you can see below, the code will print out status 
            //(#explored states, size of the frontier, #generated states, total time used) for every 10000th node generated.
            //You might also find it helpful to print out these stats when a solution has been found, so you can keep 
            //track of the exact total number of states generated.


            int iterations = 0;

            frontier.add(initialState);
            HashSet<State> explored = new HashSet<>();

            while (true) {
                //If the frontier is empty, then it returns a failure
                if(frontier.isEmpty()){
                    return null;
                }
                //Choose and remove element n from the frontier
                State n = frontier.pop();
                //Check if it is a goal state
                if(n.isGoalState()){
                    //if so, print the status and return the solution
                    printSearchStatus(explored, frontier);
                    return n.extractPlan();
                }
                //If it was not a goal state, then add the state to the explored hashset
                explored.add(n);
                //Get an array list of children of n
                ArrayList<State> children = n.getExpandedStates();
                //Loop through each child
                for(int i=0; i<children.size(); i++){
                    State m = children.get(i);
                    //If neither the frontier nor explored contain m, then m is added to the frontier
                    if(!(frontier.contains(m) || explored.contains(m))){
                        frontier.add(m);
                    }
                }
                //Print a status message every 10000 iteration
                if (++iterations % 10000 == 0) {
                    printSearchStatus(explored, frontier);
                }
                
                // //Your code here...
                // if(frontier.isEmpty())
                // {
                //     return null; // No solution exists. All possible nodes have been explored.
                // }

                // var leafNode = frontier.pop(); 
                
                // if(leafNode.isGoalState()){
                //     return leafNode.extractPlan();
                // }
                // System.out.println(leafNode.jointAction);
                // explored.add(leafNode);
                // for(var leaf : leafNode.getExpandedStates()){
                //     if(!explored.contains(leaf) && !frontier.contains(leaf) && leaf.jointAction.length == 0){
                //         frontier.add(leaf);
                //     }
                // }
            }
        }
    }

    private static long startTime = System.nanoTime();

    private static void printSearchStatus(HashSet<State> explored, Frontier frontier)
    {
        String statusTemplate = "#Expanded: %,8d, #Frontier: %,8d, #Generated: %,8d, Time: %3.3f s\n%s\n";
        double elapsedTime = (System.nanoTime() - startTime) / 1_000_000_000d;
        System.err.format(statusTemplate, explored.size(), frontier.size(), explored.size() + frontier.size(),
                          elapsedTime, Memory.stringRep());
    }
}
