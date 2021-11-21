package main;


import model.Result;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WalkSat {
    public static final int PROB_RND = 20; // (1-PROB_RND)% of the time, be greedy. (PROB_RND)% of the time, do random walk
    public static final int numVars = 20;
    public static final int VARS_PER_CLAUSE = 3; // 3SAT
    public static final Random RND = new Random();

    private static final int NUM_CLAUSE_ATTEMPTS = 10;
    private static final int CLAUSE_INCR_NUM = 20; // max num of clauses = NUM_CLAUSE_ATTEMPTS * CLAUSE_INCR_NUM
    private static final int NUM_ATTEMPTS_PER_CLAUSE = 50;
    private static final int TIMEOUT = 10; // in seconds, for each sentence that is attempted
    private static final String FILENAME = "data.csv"; // will be saved at project root, overrides existing file

    public WalkSat() throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newFixedThreadPool(6);
        List<Result> results = new ArrayList<>();
        for (int i = 0; i < NUM_CLAUSE_ATTEMPTS; i++) {
            int numClauses = (i + 1) * CLAUSE_INCR_NUM;

            System.out.println("Attempting with " + numClauses + " clauses");

            List<Integer> numFlips = new ArrayList<>();

            List<Future<String>> tasks = new ArrayList<>();
            for (int j = 0; j < NUM_ATTEMPTS_PER_CLAUSE; j++) {

                int finalJ = j;
                Callable<String> solverTask = () -> {
                    Solver solver = new Solver(numClauses);
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    Future<Integer> future = executor.submit(solver);
                    String result;
                    try {
                        int flips = future.get(TIMEOUT, TimeUnit.SECONDS);
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
            results.add(new Result(numClauses, numFlips.size(), getMedianFlips(numFlips), NUM_ATTEMPTS_PER_CLAUSE, resultsStr));
        }

        System.out.println("Done!");
        executorService.shutdownNow();

        printData(results);


        System.exit(0);
    }

    private int getMedianFlips(List<Integer> numFlips){
        return numFlips.size() == 0? 0:
                numFlips.stream().sorted().collect(Collectors.toList()).get(numFlips.size()/2);
    }

    private void printData(List<Result> results) {
        results.forEach(Result::print);
        List<String> data = new ArrayList<>();
        data.add(convertToCSV(new String[]{"Number of clauses", "Number of completed runs", "Median Number of Flips", "Total Runs", "C/N"}));
        results.stream().map(Result::getDataAsStr).map(this::convertToCSV).forEach(data::add);

        data.forEach(System.out::println);

        File outputFile = new File(FILENAME);
        try (PrintWriter pw = new PrintWriter(outputFile)){
            data.forEach(pw::println);
            System.out.println("Data saved at the project root under " + System.getProperty("user.dir") + "/" + FILENAME);
        } catch (FileNotFoundException e) {
            System.out.println("Error: could not create file: " + e.getMessage());
        }
    }


    // Taken from https://www.baeldung.com/java-csv
    public String convertToCSV(String[] data) {
        return Stream.of(data)
                .map(this::escapeSpecialCharacters)
                .collect(Collectors.joining(","));
    }

    // Taken from https://www.baeldung.com/java-csv
    public String escapeSpecialCharacters(String data) {
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }

    public static void main(String[] args){
        try {
            new WalkSat();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
