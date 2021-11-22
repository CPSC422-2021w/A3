package main;

import model.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class WalkSat {
    public static final int PROB_RND_WALK = 20; // (1-PROB_RND_WALK)% of the time, be greedy. (PROB_RND_WALK)% of the time, do random walk
    public static final int NUM_VARS = 20;
    public static final int VARS_PER_CLAUSE = 3; // 3SAT
    public static final String FILENAME = "data.csv"; // will be saved at project root, overrides existing file

    private static final int NUM_CLAUSE_ATTEMPTS = 10;
    private static final int CLAUSE_INCR_NUM = 20; // max num of clauses = NUM_CLAUSE_ATTEMPTS * CLAUSE_INCR_NUM
    private static final int NUM_ATTEMPTS_PER_CLAUSE = 50;
    private static final int TIMEOUT = 10; // in seconds, for each sentence that is attempted
    private static final int THREAD_COUNT = 6; //Only for  Higher count will lead to faster execution but a lower probability of finding solutions
    private int progress = -1;
    private int maxProgress = 0;


    // Choose .runLimitedThreads if system is CPU bound, or for more accurate results.
    //          Can take a few minutes, will use more than THREAD_COUNT Threads
    // Choose .runNoThreadLimit to use a CachedPool. Unlimited threads, but some runs may produce worse results.
    //          Will finish significantly faster, ~10-20 seconds
    public static void main(String[] args) {
        try {
            //List<Result> results = new WalkSat().runLimitedThreads();
            List<Result> results = new WalkSat().runNoThreadLimit();
            Printer.printData(results, FILENAME);
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Runs with limited thread count
    // Individual runs will still use their own threads, so overall thread count may be higher than specified
    private List<Result> runLimitedThreads() throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        maxProgress = NUM_ATTEMPTS_PER_CLAUSE;
        List<Result> results = new ArrayList<>();
        // For each Clause count (20, 40, 60...)
        for (int i = 0; i < NUM_CLAUSE_ATTEMPTS; i++) {
            int numClauses = (i + 1) * CLAUSE_INCR_NUM;
            System.out.println("Attempting with " + numClauses + " clauses");
            List<Integer> numFlips = new ArrayList<>();

            // Run algorithm NUM_ATTEMPTS_PER_CLAUSE times, get the result strings for that clause count
            List<String> resultsStr = getResults(executorService,numClauses, numFlips);

            results.add(new Result(numClauses, numFlips.size(), getMedianFlips(numFlips), NUM_ATTEMPTS_PER_CLAUSE, resultsStr));
            System.out.println();
            progress = -1;
        }
        executorService.shutdownNow();
        return results;
    }

    // Runs with no thread limit, will likely cause system to slow down
    private List<Result> runNoThreadLimit() throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newCachedThreadPool();
        List<Result> results = new ArrayList<>();
        List<Future<Result>> tasks = new ArrayList<>();
        maxProgress = NUM_ATTEMPTS_PER_CLAUSE * NUM_CLAUSE_ATTEMPTS;
        // For each Clause count (20, 40, 60...)
        for (int i = 0; i < NUM_CLAUSE_ATTEMPTS; i++) {
            int numClauses = (i + 1) * CLAUSE_INCR_NUM;

            System.out.println("Attempting with " + numClauses + " clauses");
            // Create individual threads for each clause count.
            // These will all run simultaneously
            Callable<Result> solverTasks = () -> {
                List<Integer> numFlips = new ArrayList<>();
                List<String> resultsStr = getResults(executorService, numClauses, numFlips);
                return new Result(numClauses, numFlips.size(), getMedianFlips(numFlips), NUM_ATTEMPTS_PER_CLAUSE, resultsStr);
            };
            tasks.add(executorService.submit(solverTasks));
        }

        // Wait until all threads have completed (or timeout)
        for (Future<Result> futureResult: tasks){
            results.add(futureResult.get());
        }
        executorService.shutdownNow();
        return results;
    }

    // Gets the results from each run
    // Waits for each run to complete (or to timeout)
    // Prints progress results after each run is completed
    private List<String> getResults(ExecutorService executorService, int numClauses, List<Integer> numFlips) throws InterruptedException, ExecutionException {
        calcProgress();
        List<Future<String>> tasks = new ArrayList<>();
        for (int j = 0; j < NUM_ATTEMPTS_PER_CLAUSE; j++) {
            Callable<String> solverTask = runSolvingAlgorithm(numClauses, numFlips, j);
            tasks.add(executorService.submit(solverTask));
        }
        List<String> resultsStr = new ArrayList<>();
        for (Future<String> result : tasks) {
            resultsStr.add(result.get());
            Thread.sleep(1);
            calcProgress();
        }
        return resultsStr;
    }


    // Runs the solving algorithm in Solver
    // Timeout after specified TIMEOUT.
    // Saves number of variable flips in numFlips
    // Returns a string containing the result for the current run
    private Callable<String> runSolvingAlgorithm(int numClauses, List<Integer> numFlips, int j) {
        return () -> {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<Integer> future = executor.submit(new Solver(numClauses, PROB_RND_WALK, NUM_VARS));
            try {
                int flips = future.get(TIMEOUT, TimeUnit.SECONDS);
                numFlips.add(flips);
               return "        Run #" + (j + 1) + ": num flips: " + flips;


            } catch (TimeoutException | InterruptedException | ExecutionException e) {
                return "        Run #" + (j + 1) + ": did not complete";
            }finally {
                // executes after return
                executor.shutdownNow();
            }
        };
    }

    // prints current progress on the same line
    private void calcProgress() {
        progress++;
        int curr = (progress * 100 / maxProgress);
        System.out.print('\r');
        System.out.print(curr + "% ( " + progress + " / " + maxProgress + ")");
    }

    private int getMedianFlips(List<Integer> numFlips) {
        return numFlips.size() == 0 ? 0 :
                numFlips.stream().sorted().collect(Collectors.toList()).get(numFlips.size() / 2);
    }
}
