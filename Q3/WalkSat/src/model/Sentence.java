package model;

import main.WalkSat;

import java.util.*;
import java.util.stream.Collectors;

public class Sentence {
    private final List<Clause> clauses;
    private final List<Variable> varList;
    private final int numClause;
    private final int numVars;

    public Sentence(int numClause, List<Variable> varList){
        this.numClause = numClause;
        this.numVars = varList.size();
        clauses = new ArrayList<>();
        this.varList = varList;
       generateClauses();
    }

    //Generates random clauses. Guarantees that all variable will be used if numClause >= varList.size()
    // If the number of unused variables >= number of clauses left to create, select an unused variable
    // else If the number of unused variables > 1/2 numbef of clauses left to create, 50% chance of selecting unused var
    // else select random var
    private void generateClauses(){
        List<Variable> unusedVarList = new ArrayList<>(varList);
        Set<Clause> clausesSet = new HashSet<>();
        int clausesLeft;

        while ((clausesLeft = numClause - clausesSet.size()) > 0) {
            Clause clause = new Clause();
            Variable var;
            do {
                boolean isTrue = WalkSat.RND.nextBoolean();
                if (unusedVarList.size() >= clausesLeft) {
                    var = getUnusedVar(unusedVarList, isTrue);
                }else if (unusedVarList.size() > clausesLeft/2) {
                    var = WalkSat.RND.nextBoolean() ? getRandomVar(isTrue) : getUnusedVar(unusedVarList, isTrue);
                } else{
                    var = getRandomVar(isTrue);
                }

            } while (clause.addToList(var));
            clausesSet.add(clause);
        }
        clauses.addAll(clausesSet);
    }

    private Variable getRandomVar(boolean isTrue) {
        return new Variable(varList.get(WalkSat.RND.nextInt(numVars)).getId(), isTrue);
    }

    // returns a variable that has not yet been used in any clause
    private Variable getUnusedVar(List<Variable> unusedVarList, boolean isTrue) {
        int varNum = WalkSat.RND.nextInt(unusedVarList.size());
        Variable var = new Variable(unusedVarList.get(varNum).getId(), isTrue);
        unusedVarList.remove(varNum);
        return var;
    }

    // Returns the number of clauses satisfied by the given variable
    public int getNumSatisfiedClausesByVar(Variable var){
        return (int) clauses.stream().filter(clause -> clause.isSatisfiedBy(var)).count();
    }

    //Return a list of clauses that are not satisfied by the given interpretation
    public List<Clause> getUnsatisfiedClauses(Interpretation interpretation){
        return clauses.stream().filter(clause -> !clause.isSatisfied(interpretation)).collect(Collectors.toUnmodifiableList());
    }
}
