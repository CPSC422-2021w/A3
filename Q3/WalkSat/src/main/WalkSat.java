package main;


import model.Result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class WalkSat {
    private static final int NUM_CLAUSE_ATTEMPTS = 7;
    private static final int CLAUSE_INCR_NUM = 20;
    private static final int NUM_ATTEMPTS_PER_CLAUSE_COUNT = 10;
    public static final Random RND = new Random();

    public WalkSat() throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newFixedThreadPool(6);
        List<Result> results = new ArrayList<>();
        for (int i = 0; i < NUM_CLAUSE_ATTEMPTS; i++) {
            int numClauses = (i + 1) * CLAUSE_INCR_NUM;

            System.out.println("Attempting with " + numClauses + " clauses");

            List<Integer> numFlips = new ArrayList<>();

            List<Future<String>> tasks = new ArrayList<>();
            for (int j = 0; j < NUM_ATTEMPTS_PER_CLAUSE_COUNT; j++) {

                int finalJ = j;
                Callable<String> solverTask = () -> {
                    Solver solver = new Solver(numClauses);
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    Future<Integer> future = executor.submit(solver);
                    String result;
                    try {
                        int flips = future.get(10, TimeUnit.SECONDS);
                        result = "        Run #" + (finalJ + 1) + ": num flips: " + flips;

                        numFlips.add(flips);
                    }catch (TimeoutException | InterruptedException | ExecutionException e) {
                       result = "        Run #" + (finalJ + 1) + ": did not complete";
                    }finally {
                        executor.shutdown();
                    }
                    return result;
                };


                tasks.add(executorService.submit(solverTask));
            }
            List<String> resultsStr = new ArrayList<>();
            for (Future<String> result: tasks) {
                resultsStr.add(result.get());
            }
            results.add(new Result(numClauses, numFlips.size(), getMedianFlips(numFlips), resultsStr));
        }

        System.out.println("Done!");
        executorService.shutdownNow();

        results.forEach(Result::print);
        System.exit(0);
    }

    private int getMedianFlips(List<Integer> numFlips){
        return numFlips.size() == 0? 0:
                numFlips.stream().sorted().collect(Collectors.toList()).get(numFlips.size()/2);
    }

    public static void main(String[] args){
        try {
            new WalkSat();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}