package cs345.regex;

/**
 * Represents a regular expression of the following form (ANTLR notation):
 * <p>
 * regex    : sequence ('|' sequence)* ;
 * sequence : closure sequence | ; // same as closure* but easier to build NFA
 * closure  : element '*' | element ;
 * element  : letter | '(' regex ')' ;
 * <p>
 * To learn all about regex, see Russ Cox's amazing document:
 * https://swtch.com/~rsc/regexp/regexp1.html
 */
public class Regex {
    protected String pattern;
    protected int j = 0;

    public Regex(String pattern) {
        this.pattern = pattern;
    }

    /**
     * Parse the regex pattern and return an NFA that matches strings
     * in the language described by the pattern.
     * <p>
     * See http://algs4.cs.princeton.edu/lectures/54RegularExpressions.pdf
     * https://swtch.com/~rsc/regexp/regexp1.html
     * http://www.cs.may.ie/staff/jpower/Courses/Previous/parsing/node5.html
     * <p>
     * Here are the pattern constructs to NFA conversions (x,y are themselves
     * regex patterns and o implies we construct a new state):
     * <p>
     * Empty string        o--->o
     * <p>
     * Letter a            o-a->o
     * <p>
     * xy                  o->[x]->[y]             (x.stop = y.start)
     * <p>
     * x | y               o->[x]->o
     * |       ^
     * |->[y]--|
     * <p>
     * |------|
     * v      |
     * x*                  o->[x]-|->o
     * |         ^
     * |---------|
     */
    protected NFA compile() {
        RegexParser parser = new RegexParser(pattern);
        return parser.parse();
    }

    public boolean matches(String input) {
        NFA nfa = compile();
        nfa.stop.accept(1); // mark final state as accept state for alt 1
        int alt = simulate(nfa.start, input.toCharArray(), 0);
        return alt == 1;
    }

    public int simulate(NFA.State p, char[] input, int i) {
        if (p.accept == 1) {

            if (i != input.length) {
                return NFA.FAIL;
            }
            return 1;
        }
        for (NFA.Edge e : p.edges) {

            if (e.label == NFA.EPSILON) {

                int success = simulate(e.target, input, i);

                if (success != NFA.FAIL) {
                    return success;
                }
            } else if (i < input.length && e.label == input[i]) {

                int success1 = simulate(e.target, input, i + 1);

                if (success1 != NFA.FAIL) {
                    return success1;
                }
            }
        }
        return NFA.FAIL;
    }
}
