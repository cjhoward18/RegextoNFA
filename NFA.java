package cs345.regex;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * An NFA is a collection of numbered states that have labeled, directed edges
 * between them. It's a Directed (possibly) Cyclic Graph.  An instance of
 * an NFA just tracks the outermost start and stop states of a graph.
 * Matching begins at the start state and finishes at the stop/accept state.
 */
public class NFA {
    public static final int FAIL = 0; // invalid value for accept in stop state
    public static final char EPSILON = 0;
    protected static int globalStateCounter = 0;

    public static class State {
        public final int stateNumber; // state number
        public final List<Edge> edges = new ArrayList<>();
        protected int accept = FAIL;

        public State() {
            stateNumber = globalStateCounter++;
        }

        public void edge(char label, State target) {
            Edge e = new Edge(label, target);
            edges.add(e);
        }

        public void epsilon(State target) {
            edge(EPSILON, target);
        }

        public void accept(int alt) {
            accept = alt;
        }

        public String toString() {
            return "s" + stateNumber;
        }
    }

    public static class Edge {
        State target;
        char label;

        public Edge(char label, State target) {
            this.label = label;
            this.target = target;
        }
    }

    protected State start;
    protected State stop;

    public NFA() {
        start = new State();
        stop = new State();
    }

    public NFA(NFA old) {
        this.start = old.start;
        this.stop = old.stop;
    }

    public NFA(State start, State stop) {
        this.stop = stop;
        this.start = start;
    }

    public static NFA error() {
        return atom('\uFFFF');
    }

    public String toDOT() {
        StringBuilder buf = new StringBuilder();
        Set<Integer> visited = new LinkedHashSet<>();
        buf.append("digraph nfa {\n");
        buf.append("  rankdir = LR;\n");
        buf.append("  node [shape = circle, height = 0.45, fontsize=18, fixedsize=true];\n");
        toDOT_(start, buf, visited);
        buf.append("}\n");
        return buf.toString();
    }

    protected void toDOT_(State p, StringBuilder buf, Set<Integer> visited) {

        if (visited.contains(p.stateNumber)) {
            return;
        }
        visited.add(p.stateNumber);
        for (Edge e : p.edges) {
            if (e.label == NFA.EPSILON) {
                buf.append("  " + p.stateNumber + " -> " + e.target.stateNumber + " [label=\"&epsilon;\", fontsize=18];\n");
            } else {
                buf.append("  " + p.stateNumber + " -> " + e.target.stateNumber + " [label=\"" + e.label + "\", fontsize=18];\n");
            }
            toDOT_(e.target, buf, visited);
        }

    }

    /**
     * Return an ordered set of states obtained via a depth-first walk
     */
    public LinkedHashSet<State> getStates() {
        LinkedHashSet<State> visited = new LinkedHashSet<>();
        getStates_(start, visited);
        return visited;
    }

    protected void getStates_(State p, LinkedHashSet<State> visited) {

        if (visited.contains(p)) {
            return;
        }
        visited.add(p);
        for (Edge e : p.edges) {
            getStates_(e.target, visited);
        }
    }

    public static NFA atom(char label) {
        State a = new State();
        State b = new State();
        a.edge(label, b);
        return new NFA(a, b);
    }
}
