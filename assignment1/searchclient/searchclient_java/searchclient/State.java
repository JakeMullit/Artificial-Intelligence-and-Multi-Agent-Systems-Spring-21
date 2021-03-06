package searchclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class State
{
    private static final Random RNG = new Random(1);

    /*
        The agent rows, columns, and colors are indexed by the agent number.
        For example, this.agentRows[0] is the row location of agent '0'.
    */
    public int[] agentRows;
    public int[] agentCols;
    public static Color[] agentColors;

    /*
        The walls, boxes, and goals arrays are indexed from the top-left of the level, row-major order (row, col).
               Col 0  Col 1  Col 2  Col 3
        Row 0: (0,0)  (0,1)  (0,2)  (0,3)  ...
        Row 1: (1,0)  (1,1)  (1,2)  (1,3)  ...
        Row 2: (2,0)  (2,1)  (2,2)  (2,3)  ...
        ...

        For example, this.walls[2] is an array of booleans for the third row.
        this.walls[row][col] is true if there's a wall at (row, col).
    */
    public static boolean[][] walls;
    public char[][] boxes;
    public static char[][] goals;
    public static char[][] passableGoals;

    /*
        The box colors are indexed alphabetically. So this.boxColors[0] is the color of A boxes, 
        this.boxColor[1] is the color of B boxes, etc.
    */
    public static Color[] boxColors;
 
    public final State parent;
    public final Action[] jointAction;
    private final int g;
    private int pp;
    private int movePenalties;

    private int hash = 0;

    public int f_score;


    // Constructs an initial state.
    // Arguments are not copied, and therefore should not be modified after being passed in.
    public State(int[] agentRows, int[] agentCols, Color[] agentColors, boolean[][] walls,
                 char[][] boxes, Color[] boxColors, char[][] goals
    )
    {
        this.agentRows = agentRows;
        this.agentCols = agentCols;
        this.agentColors = agentColors;
        this.walls = walls;
        this.boxes = boxes;
        this.boxColors = boxColors;
        this.goals = goals;
        this.parent = null;
        this.jointAction = null;
        this.g = 0;
        this.pp = 0;
        this.passableGoals = goals;
        this.movePenalties = 0;
    }


    // Constructs the state resulting from applying jointAction in parent.
    // Precondition: Joint action must be applicable and non-conflicting in parent state.
    private State(State parent, Action[] jointAction)
    {
        // Copy parent
        this.agentRows = Arrays.copyOf(parent.agentRows, parent.agentRows.length);
        this.agentCols = Arrays.copyOf(parent.agentCols, parent.agentCols.length);
        this.boxes = new char[parent.boxes.length][];
        for (int i = 0; i < parent.boxes.length; i++)
        {
            this.boxes[i] = Arrays.copyOf(parent.boxes[i], parent.boxes[i].length);
        }

        // Set own parameters
        this.parent = parent;
        this.jointAction = Arrays.copyOf(jointAction, jointAction.length);
        this.g = parent.g + 1;
        this.pp = parent.pp;
        this.movePenalties = parent.movePenalties;

        // Apply each action
        int numAgents = this.agentRows.length;
        for (int agent = 0; agent < numAgents; ++agent)
        {
            Action action = jointAction[agent];
            char box;
            int boxRow;
            int boxCol;
            switch (action.type)
            {
                case NoOp:
                    break;

                case Move:
                    this.movePenalties+=1;
                    this.agentRows[agent] += action.agentRowDelta;
                    this.agentCols[agent] += action.agentColDelta;
                    break;
                case Push:
                    this.pp+=1;
                    
                    //Get the box location
                    boxRow = this.agentRows[agent] + action.agentRowDelta;
                    boxCol = this.agentCols[agent] + action.agentColDelta;
                    //Get the box char
                    box = this.boxes[boxRow][boxCol];
                    //Set previous location to 0, and current location to the box char
                    this.boxes[boxRow][boxCol] = 0;
                    this.boxes[boxRow+action.boxRowDelta][boxCol+action.boxColDelta] = box;
                    //Add delta to the agent location
                    this.agentRows[agent] += action.agentRowDelta;
                    this.agentCols[agent] += action.agentColDelta;
                    resultCloserToGoal(boxRow, boxCol, boxRow+action.boxRowDelta, boxCol+action.boxColDelta, box);
                    break;
                case Pull:
                    this.pp+=1;
                    //Get the box location
                    boxRow = this.agentRows[agent] - action.boxRowDelta;
                    boxCol = this.agentCols[agent] - action.boxColDelta;
                    //Get the box char
                    box = this.boxes[boxRow][boxCol];
                    //Set previous location to 0, and current location to the box char
                    this.boxes[boxRow][boxCol] = 0;
                    this.boxes[boxRow+action.boxRowDelta][boxCol+action.boxColDelta] = box;
                    //Add delta to the agent location
                    this.agentRows[agent] += action.agentRowDelta;
                    this.agentCols[agent] += action.agentColDelta;
                    resultCloserToGoal(boxRow, boxCol, boxRow+action.boxRowDelta, boxCol+action.boxColDelta, box);
                    break;
            }
        }
    }

    public int g()
    {
        return this.g;
    }
    public int pp(){
        return this.pp;
    }
    public int movePenalties(){
        return this.movePenalties;
    }

    public boolean isGoalState()
    {
        for (int row = 1; row < this.goals.length - 1; row++)
        {
            for (int col = 1; col < this.goals[row].length - 1; col++)
            {
                char goal = this.goals[row][col];

                if ('A' <= goal && goal <= 'Z' && this.boxes[row][col] != goal)
                {
                    return false;
                }
                else if ('0' <= goal && goal <= '9' &&
                         !(this.agentRows[goal - '0'] == row && this.agentCols[goal - '0'] == col))
                {
                    return false;
                }
            }
        }
        return true;
    }
    //Method for verifying whether a push/pull resulted in a box being closer to the goal
    public void resultCloserToGoal(int oldRow, int oldCol, int newRow, int newCol, int boxChar){
        boolean foundGoal = false;
        //Loop through goals
        for (int row = 1; row < this.goals.length - 1; row++)
        {
            for (int col = 1; col < this.goals[row].length - 1; col++)
            {
                //If we have found a goal that matches the box character, stop looking for goals and give movement penalties
                if(foundGoal == false){
                char goal = this.goals[row][col];
                if(boxChar == goal){
                if ('A' <= goal && goal <= 'Z' && this.boxes[row][col] != goal)
                {
                    foundGoal = true;
                    //The delta of the box movement
                    int changeRow = newRow - oldRow;
                    int changeCol = newCol - oldCol;
                    //If the goal row is higher that than the old box position, that means the box needs to be moved to the right, i.e. delta = 1
                    if(row>oldRow){
                        //If the delta is higher, we can remove a penalty point
                        if(changeRow>0){
                            this.movePenalties = this.movePenalties - 1;
                        }
                        //If the delta is lower, we add a penalty
                        if(changeRow<0){
                            this.movePenalties = this.movePenalties + 1;
                        }
                    }
                    //If the goal row is smaller than the old box position, that means the box needs to be moved to the left, i.e. delta = -1
                    else if(row<oldRow){
                        //If the delta is lower, we can remove a penalty point
                        if(changeRow<0){
                            this.movePenalties = this.movePenalties - 1;
                        }
                        //If the delta is higher, we add a penalty
                        if(changeRow>0){
                            this.movePenalties = this.movePenalties + 1;
                        }
                    }
                    //If the goal col is higher that than the old box position, that means the box needs to be moved down, i.e. delta = 1
                    if(col>oldCol){
                        //If the delta is higher, we can remove a penalty point
                        if(changeCol>0){
                            this.movePenalties = this.movePenalties - 1;
                        }
                        //If the delta is lower, we add a penalty
                        if(changeCol<0){
                            this.movePenalties = this.movePenalties + 1;
                        }
                    }
                    //If the goal col is higher that than the old box position, that means the box needs to be moved up, i.e. delta = -1
                    else if(col<oldCol){
                        //If the delta is lower, we can remove a penalty point
                        if(changeCol<0){
                            this.movePenalties = this.movePenalties - 1;
                        }
                        //If the delta is higher, we add a penalty
                        if(changeCol>0){
                            this.movePenalties = this.movePenalties + 1;
                        }
                    }
                }
                }
            }
            }
        }
    }

    public ArrayList<State> getExpandedStates()
    {
        int numAgents = this.agentRows.length;

        // Determine list of applicable actions for each individual agent.
        Action[][] applicableActions = new Action[numAgents][];
        for (int agent = 0; agent < numAgents; ++agent)
        {
            ArrayList<Action> agentActions = new ArrayList<>(Action.values().length);
            for (Action action : Action.values())
            {
                if (this.isApplicable(agent, action))
                {
                    agentActions.add(action);
                }
            }
            applicableActions[agent] = agentActions.toArray(new Action[0]);
        }

        // Iterate over joint actions, check conflict and generate child states.
        Action[] jointAction = new Action[numAgents];
        int[] actionsPermutation = new int[numAgents];
        ArrayList<State> expandedStates = new ArrayList<>(16);
        while (true)
        {
            for (int agent = 0; agent < numAgents; ++agent)
            {
                jointAction[agent] = applicableActions[agent][actionsPermutation[agent]];
            }

            if (!this.isConflicting(jointAction))
            {
                expandedStates.add(new State(this, jointAction));
            }

            // Advance permutation
            boolean done = false;
            for (int agent = 0; agent < numAgents; ++agent)
            {
                if (actionsPermutation[agent] < applicableActions[agent].length - 1)
                {
                    ++actionsPermutation[agent];
                    break;
                }
                else
                {
                    actionsPermutation[agent] = 0;
                    if (agent == numAgents - 1)
                    {
                        done = true;
                    }
                }
            }

            // Last permutation?
            if (done)
            {
                break;
            }
        }

        Collections.shuffle(expandedStates, State.RNG);
        return expandedStates;
    }

    private boolean isApplicable(int agent, Action action)
    {
        int agentRow = this.agentRows[agent];
        int agentCol = this.agentCols[agent];
        Color agentColor = this.agentColors[agent];
        int boxRow;
        int boxCol;
        char box;
        int destinationRow;
        int destinationCol;
        int currBoxRow;
        int currBoxCol;
        switch (action.type)
        {
            case NoOp:
                return true;

            case Move:
                destinationRow = agentRow + action.agentRowDelta;
                destinationCol = agentCol + action.agentColDelta;
                return this.cellIsFree(destinationRow, destinationCol);
            case Push:
                //Destination cell of the agent
                destinationRow = agentRow + action.agentRowDelta;
                destinationCol = agentCol + action.agentColDelta;
                //Current box cell, same as the agent destination
                currBoxRow = destinationRow;
                currBoxCol = destinationCol;
                //Destination cell of the box
                boxRow = currBoxRow + action.boxRowDelta;
                boxCol = currBoxCol + action.boxColDelta;
                //if the destination cell contains a box
                if(this.containsBox(destinationRow, destinationCol)){
                    //get the box index
                    int boxIndex = this.boxAt(destinationRow, destinationCol);
                    if(boxIndex == -1){
                        return false;
                    }
                    //check if the colors match
                    Color boxColor = boxColors[boxIndex];
                    if(boxColor == agentColor){
                        //return if the destination cell of the box is free
                        return this.cellIsFree(boxRow, boxCol);
                    }
                }
                return false;
            case Pull:
                //Destination of the agent
                destinationRow = agentRow + action.agentRowDelta;
                destinationCol = agentCol + action.agentColDelta;
                //Current box location
                currBoxRow = agentRow - action.boxRowDelta;
                currBoxCol = agentCol - action.boxColDelta;
                //Destination cell of the box, same as the current agent location
                boxRow = agentRow;
                boxCol = agentCol;
                //If the cell contains a box
                if(this.containsBox(currBoxRow, currBoxCol)){
                    //get the box index
                    int boxIndex = this.boxAt(currBoxRow, currBoxCol);
                    if(boxIndex == -1){
                        return false;
                    }
                    //check if the colors match
                    Color boxColor = boxColors[boxIndex];
                    if(boxColor == agentColor){
                        //return if the destination cell of the agent is free
                        return this.cellIsFree(destinationRow, destinationCol);
                    } 
                }
                return false;
        }

        // Unreachable:
        return false;
    }

    private boolean isConflicting(Action[] jointAction)
    {
        int numAgents = this.agentRows.length;

        int[] destinationRows = new int[numAgents]; // row of new cell to become occupied by action
        int[] destinationCols = new int[numAgents]; // column of new cell to become occupied by action
        int[] boxRows = new int[numAgents]; // current row of box moved by action
        int[] boxCols = new int[numAgents]; // current column of box moved by action

        // Collect cells to be occupied and boxes to be moved
        for (int agent = 0; agent < numAgents; ++agent)
        {
            Action action = jointAction[agent];
            int agentRow = this.agentRows[agent];
            int agentCol = this.agentCols[agent];
            int boxRow;
            int boxCol;
            int currBoxRow;
            int currBoxCol;

            switch (action.type)
            {
                case NoOp:
                    break;

                case Move:
                    destinationRows[agent] = agentRow + action.agentRowDelta;
                    destinationCols[agent] = agentCol + action.agentColDelta;
                    boxRows[agent] = agentRow; // Distinct dummy value
                    boxCols[agent] = agentCol; // Distinct dummy value
                    break;
                case Push:
                    //The destination cell of an agent
                    destinationRows[agent] = agentRow + action.agentRowDelta;
                    destinationCols[agent] = agentCol + action.agentColDelta;
                    //The current cell of a box
                    currBoxRow = destinationRows[agent];
                    currBoxCol = destinationCols[agent];
                    //The destination cell of a box
                    boxRows[agent] = currBoxRow+action.boxRowDelta;
                    boxCols[agent] = currBoxCol+action.boxColDelta;
                    break;
                case Pull:
                    //The destination cell of an agent
                    destinationRows[agent] = agentRow + action.agentRowDelta;
                    destinationCols[agent] = agentCol + action.agentColDelta;
                    //The destination cell of a box. The same as the current location of the agent
                    boxRows[agent] = agentRows[agent];
                    boxCols[agent] = agentCols[agent];
                    //The current cell of a box. 
                    currBoxRow = boxRows[agent] - action.boxRowDelta;
                    currBoxCol = boxCols[agent] - action.boxColDelta;
                    break;
           }
        }

        for (int a1 = 0; a1 < numAgents; ++a1)
        {
            if (jointAction[a1] == Action.NoOp)
            {
                continue;
            }

            for (int a2 = a1 + 1; a2 < numAgents; ++a2)
            {
                if (jointAction[a2] == Action.NoOp)
                {
                    continue;
                }

                // Moving into same cell?
                if (destinationRows[a1] == destinationRows[a2] && destinationCols[a1] == destinationCols[a2])
                {
                    return true;
                }
                //Agent moving into a box?
                else if (destinationRows[a1] == boxRows[a2] && destinationCols[a1] == boxCols[a2]){
                    return true;
                }
                //A box moving into an agent?
                else if (boxRows[a1] == destinationRows[a2] && boxCols[a1] == destinationCols[a2]){
                    return true;
                }
                //A box moving into a box?
                else if(boxRows[a1] == boxRows[a2] && boxCols[a1] == boxCols[a2]){
                    return true;
                }

            }
        }

        return false;
    }

    // Method for checking whether a cell contains a box
    private boolean containsBox(int row, int col){
        return this.boxes[row][col]!=0;

    }
    //Method for retrieving the index of the box, based on the row and col it belongs at.
    private int boxAt(int row, int col){
        if(this.boxes[row][col]!=0){
            return getIndexFromChar(this.boxes[row][col]);
        }
        return -1;
    }
    //Method for converting a character to an integer. Used to identify the index of the boxes.
    private int getIndexFromChar(char character){
        switch(Character.toLowerCase(character)){
            case 'a':
                return 0;
            case 'b':
                return 1;
            case 'c':
                return 2;
            case 'd':
                return 3;
            case 'e':
                return 4;
            case 'f':
                return 5;
            case 'g':
                return 6;
            case 'h':
                return 7;
            case 'i':
                return 8;
            default:
                return -1;
        }
    }

    //Method to retrieve the amount of goals
    public int getGoals(){
        int totalGoals = 0;
        char[][] passedGoals = this.passableGoals;
        //Loop through the goals array and count the goals
        for (int i=0; i < passedGoals.length; i++){
            for (int j=0; j < passedGoals[i].length; j++){
                char curGoal = passedGoals[i][j];
                if ('A' <= curGoal && curGoal <= 'Z' && this.boxes[i][j] != curGoal)
                {
                    totalGoals++;
                }
                else if ('0' <= curGoal && curGoal <= '9' &&
                        !(this.agentRows[curGoal - '0'] == i && this.agentCols[curGoal - '0'] == j))
                {
                    totalGoals++;
                }
            }
        }
        return totalGoals;
    }

    private boolean cellIsFree(int row, int col)
    {
        return !this.walls[row][col] && this.boxes[row][col] == 0 && this.agentAt(row, col) == 0;
    }
    
    

    private char agentAt(int row, int col)
    {
        for (int i = 0; i < this.agentRows.length; i++)
        {
            if (this.agentRows[i] == row && this.agentCols[i] == col)
            {
                return (char) ('0' + i);
            }
        }
        return 0;
    }

    public Action[][] extractPlan()
    {
        Action[][] plan = new Action[this.g][];
        State state = this;
        while (state.jointAction != null)
        {
            plan[state.g - 1] = state.jointAction;
            state = state.parent;
        }
        return plan;
    }

    @Override
    public int hashCode()
    {
        if (this.hash == 0)
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(this.agentColors);
            result = prime * result + Arrays.hashCode(this.boxColors);
            result = prime * result + Arrays.deepHashCode(this.walls);
            result = prime * result + Arrays.deepHashCode(this.goals);
            result = prime * result + Arrays.hashCode(this.agentRows);
            result = prime * result + Arrays.hashCode(this.agentCols);
            for (int row = 0; row < this.boxes.length; ++row)
            {
                for (int col = 0; col < this.boxes[row].length; ++col)
                {
                    char c = this.boxes[row][col];
                    if (c != 0)
                    {
                        result = prime * result + (row * this.boxes[row].length + col) * c;
                    }
                }
            }
            this.hash = result;
        }
        return this.hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (this.getClass() != obj.getClass())
        {
            return false;
        }
        State other = (State) obj;
        return Arrays.equals(this.agentRows, other.agentRows) &&
               Arrays.equals(this.agentCols, other.agentCols) &&
               Arrays.equals(this.agentColors, other.agentColors) &&
               Arrays.deepEquals(this.walls, other.walls) &&
               Arrays.deepEquals(this.boxes, other.boxes) &&
               Arrays.equals(this.boxColors, other.boxColors) &&
               Arrays.deepEquals(this.goals, other.goals);
    }

    @Override
    public String toString()
    {
        StringBuilder s = new StringBuilder();
        for (int row = 0; row < this.walls.length; row++)
        {
            for (int col = 0; col < this.walls[row].length; col++)
            {
                if (this.boxes[row][col] > 0)
                {
                    s.append(this.boxes[row][col]);
                }
                else if (this.walls[row][col])
                {
                    s.append("+");
                }
                else if (this.agentAt(row, col) != 0)
                {
                    s.append(this.agentAt(row, col));
                }
                else
                {
                    s.append(" ");
                }
            }
            s.append("\n");
        }
        return s.toString();
    }
}
