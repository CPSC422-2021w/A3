package model;

import java.util.*;
import java.util.stream.Collectors;

public class Interpretation {
    private final Map<Integer, Variable> varMap;
    public Interpretation(int numVars, Random rnd){
        this.varMap = new HashMap<>();
        generateVars(numVars, rnd);
    }

    private void generateVars(int numVars, Random rnd){
        for (int i = 0; i < numVars; i++) {
            boolean isTrue = rnd.nextBoolean();
            int id = i * rnd.nextInt(100);
            Variable var = new Variable(id, isTrue);
            varMap.put(id, var);
        }
    }

    public List<Variable> getVarList() {
        return varMap.values().stream().collect(Collectors.toUnmodifiableList());
    }

    public boolean isVarTrue(Variable var){
        return varMap.get(var.getId()).isTrue();
    }

    public void flip(Variable var){
        varMap.get(var.getId()).flip();
    }
}
