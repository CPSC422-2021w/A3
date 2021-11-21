package model;

import java.util.List;

public class Result {

    private final int numClauses;
    private final int numCompletedRuns;
    private final int medianNumFlips;
    private final String cn;
    private final List<String> results;

    public Result(int numClauses, int numCompletedRuns, int medianFlips, List<String> results) {
        this.numClauses = numClauses;
        this.numCompletedRuns = numCompletedRuns;
        this.medianNumFlips = medianFlips;
        this.cn = "C/N = " + numClauses/20;
        this.results = results;
    }

    public void print(){
        System.out.println("Num of clauses: " + numClauses + ". " + cn);
        System.out.println("-----------------------------------------");
        for (String result: results){
            System.out.println(result);
        }
        System.out.println("Completed Runs: " + numCompletedRuns + ", Median Flip Count: " + medianNumFlips);
        System.out.println("=========================");
        System.out.println();
        System.out.println();
    }
}
