package main;

import model.Clause;
import model.Interpretation;
import model.Sentence;
import model.Variable;

import java.util.*;
import java.util.concurrent.Callable;

public class Solver implements Callable<Integer> {
    private static final int PROB_RND = WalkSat.PROB_RND; // (1-PROB_RND)% of the time, be greedy. (PROB_RND)% of the time, do random walk
    private static final int numVars = WalkSat.numVars;
    private final Sentence sentence;
    private final Interpretation interpretation;
    private final Map<Variable, Integer> numSatisfiedMap;

    public Solver(int numClauses) {
        interpretation = new Interpretation(numVars);
        List<Variable> varList = new ArrayList<>(interpretation.getVarList());
        sentence = new Sentence(numClauses, varList);
        numSatisfiedMap = new HashMap<>();
    }

    // Loop until all clauses are satisfied
    // Select a random clause, then with Prob(PROB_RND) select a random var and flip it,
    // otherwise, select the var that yields highest improvement in # of satisfied clauses and flip it
    // Returns number of flips performed
    public int solve() {
        int numFlips = 0;
        List<Clause> unsatisfiedClauses = sentence.getUnsatisfiedClauses(interpretation);

        while (!unsatisfiedClauses.isEmpty()) {
            int nextClause = WalkSat.RND.nextInt(unsatisfiedClauses.size());
            Clause clause = unsatisfiedClauses.get(nextClause);
            Variable varToFlip;
            if (WalkSat.RND.nextInt(100) < PROB_RND) {
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

    // Iterate over vars in given clause,
    // return var to flip with most improved number of satisifed clauses
    // Returns null if no var can improve current count of satisfied clauses
    private Variable getBestVarToFlip(Clause clause){
        Variable varToFlip = null;
        int max = 0;
        for (Variable var : clause) {
            int currSatisfied = getNumClauseSatisfied(var);
            Variable tempVar = new Variable(var.getId(), !interpretation.isVarTrue(var));
            int satisfied = getNumClauseSatisfied(tempVar) - currSatisfied;
            if (satisfied < 0) continue; // current interpretation is better

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

