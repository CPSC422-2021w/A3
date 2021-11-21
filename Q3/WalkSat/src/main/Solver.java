package main;

import model.Clause;
import model.Interpretation;
import model.Sentence;
import model.Variable;

import java.util.*;
import java.util.concurrent.Callable;

public class Solver implements Callable<Integer> {
    private static final int PROB_RND = 0; // 1-PROB_RND% of the time, be greedy. PROB_RND% of the time, do random walk
    private static final int numVars = 20;
    private final Sentence sentence;
    private final Interpretation interpretation;
    private final List<Variable> varList;
    private final Map<Variable, Integer> numSatisfiedMap;

    public Solver(int numClauses) {
        interpretation = new Interpretation(numVars);
        this.varList = new ArrayList<>(interpretation.getVarList());
        sentence = new Sentence(numClauses, varList);
        numSatisfiedMap = new HashMap<>();
    }

    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(sentence);
        str.append("\nInterpretation: \n");
        for (Variable var : varList) {
            str.append(var);
            str.append(" -- numSat: ");
            str.append(sentence.getNumSatisfiedClausesByVar(var));
            str.append("\n");
        }

        return str.toString();
    }

    public int solve() {
        int numFlips = 0;
        List<Clause> unsatisfiedClauses = sentence.getUnsatisfiedClauses(interpretation);

        while (unsatisfiedClauses.size() != 0) {
            int nextClause = WalkSat.RND.nextInt(unsatisfiedClauses.size());
            Clause clause = unsatisfiedClauses.get(nextClause);
            Variable varToFlip;
            if (WalkSat.RND.nextInt(100) > PROB_RND) {
                varToFlip = clause.getRandomVar();
            } else {
                varToFlip = getBestVarToFlip(clause);
                if (varToFlip == null) continue; // do not flip if doing so does not improve satisfied clause count
            }
            numFlips++;
            interpretation.flip(varToFlip);
            unsatisfiedClauses = sentence.getUnsatisfiedClauses(interpretation);
        }
        return numFlips;
    }

    private Variable getBestVarToFlip(Clause clause){
        Variable varToFlip = null;
        int max = 0;
        for (Variable var : clause) {
            int currSatisfied = getNumClauseSatisfied(var);
            Variable tempVar = new Variable(var.getId(), !interpretation.isVarTrue(var));
            int satisfied = getNumClauseSatisfied(tempVar) - currSatisfied;
            if (satisfied < 0) continue;

            if (satisfied > max) {
                max = satisfied;
                varToFlip = tempVar;
            }
        }
        return max > 0? varToFlip: null;
    }

    // Get number of clauses satisfied by a variable
    // Check map first in case result was already calculated
    private int getNumClauseSatisfied(Variable variable) {
        int satisfied;
        if (numSatisfiedMap.containsKey(variable)){
            satisfied = numSatisfiedMap.get(variable);
        }else{
            satisfied = sentence.getNumSatisfiedClausesByVar(variable);
            numSatisfiedMap.put(variable, satisfied);
        }
        return satisfied;
    }

    @Override
    public Integer call() {
        return solve();
    }
}

