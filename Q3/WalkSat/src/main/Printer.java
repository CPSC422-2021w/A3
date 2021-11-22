package main;

import model.Result;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Printer {
    public static void printData(List<Result> results, String filename) {
        results.forEach(Result::print);
        List<String> data = new ArrayList<>();
        data.add(convertToCSV(new String[]{"Number of clauses", "Number of completed runs", "Median Number of Flips", "Total Runs", "C/N"}));
        results.stream().map(Result::getDataAsStr).map(Printer::convertToCSV).forEach(data::add);

        System.out.println("Data in CSV File:");
        data.forEach(System.out::println);

        File outputFile = new File(filename);
        try (PrintWriter pw = new PrintWriter(outputFile)){
            data.forEach(pw::println);
            char slash = System.getProperty("os.name").toLowerCase().contains("windows")? '\\' : '/';
            System.out.println("Data saved at the project root under " + System.getProperty("user.dir") + slash + filename);
        } catch (FileNotFoundException e) {
            System.out.println("Error: could not create file: " + e.getMessage());
        }
    }


    // Taken from https://www.baeldung.com/java-csv
    public static String convertToCSV(String[] data) {
        return Stream.of(data)
                .map(Printer::escapeSpecialCharacters)
                .collect(Collectors.joining(","));
    }

    // Taken from https://www.baeldung.com/java-csv
    public static String escapeSpecialCharacters(String data) {
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }
}
