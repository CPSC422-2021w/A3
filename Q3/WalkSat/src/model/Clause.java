package model;

import main.WalkSat;

import java.util.*;

public class Clause implements Iterable<Variable>{

    public static final int VARS_PER_CLAUSE = WalkSat.VARS_PER_CLAUSE;
    private final Map<Integer, Variable> varMap;
    private final List<Integer> indexToIDList;
    private final Random rnd;

    public Clause(Random rnd){
        this.rnd = rnd;
        this.varMap = new HashMap<>();
        this.indexToIDList = new ArrayList<>();
    }

    // Adds given variable in this clause if it is not already present && map is not full
    // Returns false if list is full
    public boolean addToList(Variable var){
        if (varMap.size() < VARS_PER_CLAUSE && !varMap.containsKey(var.getId())) {
            varMap.put(var.getId(), var);
            indexToIDList.add(var.getId());
        }
        return varMap.size() < VARS_PER_CLAUSE;
    }

    // Returns true if any of the variables in the given interpretation satisfy this clause
    public boolean isSatisfied(Interpretation interpretation){
      return varMap.values().stream().anyMatch(variable -> interpretation.isVarTrue(variable) == variable.isTrue());
    }

    // Returns true if this clause contains the variable and is satisfied by the variable
    public boolean isSatisfiedBy(Variable var){
        return containsVar(var) && varMap.get(var.getId()).isTrue() == var.isTrue();
    }

    public boolean containsVar(Variable var){
        return varMap.containsKey(var.getId());
    }

    public Variable getRandomVar(){
        return varMap.get(indexToIDList.get(rnd.nextInt(VARS_PER_CLAUSE)));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Clause clause = (Clause) o;
        return varMap.equals(clause.varMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(varMap);
    }

    @Override
    public Iterator<Variable> iterator() {
        return varMap.values().iterator();
    }
}
