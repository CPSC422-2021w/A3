package model;

import main.WalkSat;

import java.util.*;

public class Interpretation {
    private final Map<Integer, Variable> varMap;
    public Interpretation(int numVars){
        this.varMap = new HashMap<>();
        generateVars(numVars);
    }

    private void generateVars(int numVars){
        for (int i = 0; i < numVars; i++) {
            boolean isTrue = WalkSat.RND.nextBoolean();
            Variable var = new Variable(i, isTrue);
            varMap.put(i, var);
        }
    }

    public Collection<Variable> getVarList() {
        return Collections.unmodifiableCollection(varMap.values());
    }

    public boolean isVarTrue(Variable variable){
        return varMap.get(variable.getId()).isTrue();
    }

    public void flip(Variable var){
        varMap.get(var.getId()).flip();
    }


    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        str.append("Interpretation: ");
        for (Variable var: varMap.values()){
            str.append(var);
            str.append(", ");
        }

        return str.toString();
    }
}
