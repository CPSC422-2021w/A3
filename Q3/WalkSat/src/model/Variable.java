package model;

import java.util.Objects;

public class Variable {
    private boolean isTrue;
    private final int id;

    public Variable(Integer name, boolean isTrue){
        this.id = name;
        this.isTrue = isTrue;
    }

    public void flip(){
        isTrue = !isTrue;
    }

    public boolean isTrue(){
        return isTrue;
    }

    public int getId(){
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Variable variable = (Variable) o;
        return isTrue == variable.isTrue && id == variable.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isTrue, id);
    }

    @Override
    public String toString() {
        return isTrue ? "" + id: "~" + id;
    }
}