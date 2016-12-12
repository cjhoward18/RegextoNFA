package cs345.regex;

import java.util.ArrayList;
import java.util.List;

public class RegexParser {
    protected final char[] pattern;    // the regex pattern to parse
    protected final int n;
    protected final List<String> errors = new ArrayList<>();
    protected int p = 0;            // pattern[p] is next char to match

    public RegexParser(String pattern) {
        this.pattern = pattern.toCharArray();
        n = this.pattern.length;
    }

    public List<String> getErrors() {
        return errors;
    }

    public NFA parse() {
        NFA nfa = regex();
        if (p < n) {
            error("unrecognized input");
            return NFA.error();
        }
        return nfa;
    }

    // P a r s e  m e t h o d s

    /**
     * Parse regex : sequence ('|' sequence)* ;
     */
    public NFA regex() {
        NFA done = sequence();

        if (look() == '|') {
            NFA n = new NFA();
            n.start.epsilon(done.start);
            done.stop.epsilon(n.stop);

            while (look() == '|') {
                consume();
                NFA it = sequence();
                it.stop.edge(NFA.EPSILON, n.stop);
                n.start.edge(NFA.EPSILON, it.start);
            }
            return n;
        }
        return done;
    }

    /**
     * Parse sequence : closure sequence | ;
     */
    public NFA sequence() {
        NFA back;
        NFA ok;

        if (look() >= 'a' && look() <= 'z' || look() == '(') {
            back = closure();

            if (look() != (char) -1 && look() != ')' && look() != '|') {
                ok = sequence();
                back.stop.epsilon(ok.start);
                back.stop = ok.stop;
            }
            return back;
        } else {
            back = new NFA();
            back.start.edge(NFA.EPSILON, back.stop);
            return back;
        }

    }

    /**
     * Parse closure : element '*' | element ;
     */
    public NFA closure() {
        NFA mid = element();

        if (look() == '*') {
            matchRange('*', '*');
            NFA n = new NFA();
            n.start.epsilon((mid.start));
            n.start.epsilon(n.stop);
            mid.stop.epsilon(n.start);
            mid.stop.edge(NFA.EPSILON, n.stop);
            return n;
        }
        return mid;
    }

    /**
     * Parse element : letter | '(' regex ')' ;
     */
    public NFA element() {
        NFA seed;
        if (look() == '(') {
            matchRange('(', '(');
            seed = regex();
            matchRange(')', ')');
        } else if (look() >= 'a' && look() <= 'z') {
            seed = NFA.atom(look());
            matchRange('a', 'z');
        } else {
            return NFA.error();
        }
        return seed;
    }

    // S u p p o r t

    public void consume() {
        p++;
    }

    public void error(String msg) {
        StringBuilder buf = new StringBuilder();
        buf.append(msg + " in " + new String(pattern));
        buf.append("\n");
        int spaces = p + msg.length() + " in ".length();
        for (int i = 0; i <= spaces; i++) buf.append(" ");
        buf.append("^");
        errors.add(buf.toString());
    }

    public char look() {
        if (p >= pattern.length) {
            return (char) -1;
        }
        return pattern[p];
    }

    public void matchRange(char from, char to) {
        if (look() >= from && look() <= to) {
            consume();
        } else if (look() == (char) -1) {
            error("expected " + (char) from + " at EOF");
        } else {
            error("unrecognized input");
        }
    }
}
