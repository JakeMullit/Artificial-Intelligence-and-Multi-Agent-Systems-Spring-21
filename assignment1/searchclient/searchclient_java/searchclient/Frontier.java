package searchclient;

import java.util.ArrayDeque;
import java.util.HashSet;

public interface Frontier
{
    void add(State state);
    State pop();
    boolean isEmpty();
    int size();
    boolean contains(State state);
    String getName();
}

class FrontierBFS
        implements Frontier
{
    private final ArrayDeque<State> queue = new ArrayDeque<>(65536);
    private final HashSet<State> set = new HashSet<>(65536);

    @Override
    public void add(State state)
    {
        this.queue.addLast(state);
        this.set.add(state);
    }

    @Override
    public State pop()
    {
        State state = this.queue.pollFirst();
        this.set.remove(state);
        return state;
    }

    @Override
    public boolean isEmpty()
    {
        return this.queue.isEmpty();
    }

    @Override
    public int size()
    {
        return this.queue.size();
    }

    @Override
    public boolean contains(State state)
    {
        return this.set.contains(state);
    }

    @Override
    public String getName()
    {
        return "breadth-first search";
    }
}

class FrontierDFS
        implements Frontier
{
    @Override
    public void add(State state)
    {
        throw new NotImplementedException();
    }

    @Override
    public State pop()
    {
        throw new NotImplementedException();
    }

    @Override
    public boolean isEmpty()
    {
        throw new NotImplementedException();
    }

    @Override
    public int size()
    {
        throw new NotImplementedException();
    }

    @Override
    public boolean contains(State state)
    {
        throw new NotImplementedException();
    }

    @Override
    public String getName()
    {
        return "depth-first search";
    }
}

class FrontierBestFirst
        implements Frontier
{
    private Heuristic heuristic;
    private final ArrayDeque<State> queue = new ArrayDeque<>(65536);
    //    private final PriorityQueue<State> sortedQueue = new PriorityQueue<State>();
    private final HashSet<State> set = new HashSet<>(65536);

    public int compareStates(State i, State j){
        if (i.f_score > j.f_score){
            return 1;
        }
        else if(i.f_score < j.f_score){
            return -1;
        }
        else return 0;
    }



    public FrontierBestFirst(Heuristic h)
    {
        this.heuristic = h;


    }

    @Override
    public void add(State state)
    {
//        State[] childrenStates = new State[1000];
//        int i=0;
//        for(State m : state.getExpandedStates()){
//            childrenStates[i] = m;
//        }


        this.queue.add(state);
//        this.sortedQueue.add(state);
        this.set.add(state);

//        Collections.sort(queue, this.heuristic);

//        Collections.sort(state, heuristic.compare(state, state.getExpandedStates()));
    }

    @Override
    public State pop()
    {
        State state = this.queue.poll();
//        for(State m : state.getExpandedStates()){
//            State temp = m;
////            int cost_h = this.heuristic.h(temp);
//            int temp_g_score = temp.g();
//            int temp_f_score = temp_g_score + this.heuristic.h(temp);
//
//
//        }

        this.set.remove(state);
        return state;
    }

    @Override
    public boolean isEmpty()
    {
        return this.queue.isEmpty();
    }

    @Override
    public int size()
    {
        return this.queue.size();
    }

    @Override
    public boolean contains(State state)
    {
        return this.set.contains(state);
    }

    @Override
    public String getName()
    {
        return String.format("best-first search using %s", this.heuristic.toString());
    }
}
