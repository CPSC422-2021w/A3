package model;

import main.WalkSat;

import java.util.*;

public class Clause implements Iterable<Variable>{

    public static final int VARS_PER_CLAUSE = 3;
    private final Map<Integer, Variable> varMap;
    private final List<Integer> indexToIDList;

    public Clause(){
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
        return varMap.get(indexToIDList.get(WalkSat.RND.nextInt(VARS_PER_CLAUSE)));
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        str.append("(");
        int ctr = 0;
        for (Variable var: varMap.values()) {
            str.append(var.toString());
            if (ctr < 2) {
                str.append(" v ");
            }
            ctr++;
        }
        str.append(")");

        return str.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Clause clause = (Clause) o;
        return Objects.equals(toString(), clause.toString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(toString());
    }

    @Override
    public Iterator<Variable> iterator() {
        return varMap.values().iterator();
    }
}
