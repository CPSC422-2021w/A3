package model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Result {

    private final int numClauses;
    private final int numCompletedRuns;
    private final int medianNumFlips;
    private final int totalRunCount;
    private final String cn;
    private final List<String> results;

    public Result(int numClauses, int numCompletedRuns, int medianFlips, int totalRunCount, List<String> results) {
        this.numClauses = numClauses;
        this.numCompletedRuns = numCompletedRuns;
        this.medianNumFlips = medianFlips;
        this.totalRunCount = totalRunCount;
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

    public String [] getDataAsStr(){
        return new String[]{String.valueOf(numClauses), String.valueOf(numCompletedRuns), String.valueOf(medianNumFlips), String.valueOf(totalRunCount), cn};
    }
}
