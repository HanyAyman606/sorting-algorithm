package com.example.sortingalgorithm.service;

import com.example.sortingalgorithm.algorithms.*;
import com.example.sortingalgorithm.dto.*;
import com.example.sortingalgorithm.tracker.BenchmarkTracker;
import com.example.sortingalgorithm.tracker.EinSehrya;
import com.example.sortingalgorithm.tracker.VisualizationTracker;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
public class SortingService {

    // ─────────────────────────────────────────────
    // VISUALIZATION
    // ─────────────────────────────────────────────
    public VisualizationResponse runVisualization(VisualizationRequest request) {
        // Step 1: Get array
        int[] arr1;
        if (request.getInputType().equals("file")) {
            arr1 = parseFileContent(request.getFileContent());
        } else {
            arr1 = generateArray(request.getSize(), request.getGenerationMode());
        }

        // Step 2: Save original array for frontend
        int[] originalArray = Arrays.copyOf(arr1, arr1.length);

        // Step 3: Create visualization tracker
        VisualizationTracker tracker = new VisualizationTracker();

        // Step 4: Run sort
        runSort(request.getAlgorithm(), arr1, tracker);

        // Step 5: Build response
        VisualizationResponse response = new VisualizationResponse();
        response.setSteps(tracker.getSteps());
        response.setTotalComparisons(tracker.getComparisons());
        response.setTotalInterchanges(tracker.getInterchanges());
        response.setOriginalArray(originalArray);

        return response;
    }

    // ─────────────────────────────────────────────
    // BENCHMARK
    // ─────────────────────────────────────────────
    public List<BenchmarkResponse> runBenchmark(BenchmarkRequest request) {
        List<BenchmarkResponse> results = new ArrayList<>();

        // Get selected algorithms from checkboxes
        List<String> algorithms = request.getAlgorithms();

        // Get array — single file OR generated
        int[] originalArray;
        String modeName;

        if (request.getInputType().equals("file")) {
            originalArray = parseFileContent(request.getFileContent());
            modeName = request.getFileName();
        } else {
            originalArray = generateArray(request.getSize(), request.getGenerationMode());
            modeName = request.getGenerationMode();
        }

        // Run each selected algorithm
        for (String algo : algorithms) {
            BenchmarkResponse res = runSingleBenchmark(
                algo, originalArray, request.getRuns(), modeName
            );
            results.add(res);
        }

        return results;
    }

    // ─────────────────────────────────────────────
    // SINGLE BENCHMARK RUN
    // ─────────────────────────────────────────────
    private BenchmarkResponse runSingleBenchmark(
        String algo, int[] originalArray, int runs, String modeName
    ) {
        double[] times = new double[runs];
        long comparisons = 0;
        long interchanges = 0;

        for (int i = 0; i < runs; i++) {
            int[] arr1 = Arrays.copyOf(originalArray, originalArray.length);
            BenchmarkTracker tracker = new BenchmarkTracker();

            long start = System.nanoTime();
            runSort(algo, arr1, tracker);
            long end = System.nanoTime();

            times[i] = (end - start) / 1_000_000.0;

            if (i == 0) {
                comparisons = tracker.getComparisons();
                interchanges = tracker.getInterchanges();
            }
        }

        // Calculate avg, min, max
        double avg = 0, min = times[0], max = times[0];
        for (double t : times) {
            avg += t;
            if (t < min) min = t;
            if (t > max) max = t;
        }
        avg = avg / runs;

        BenchmarkResponse response = new BenchmarkResponse();
        response.setAlgorithm(algo);
        response.setArraySize(originalArray.length);
        response.setGenerationMode(modeName);
        response.setRuns(runs);
        response.setAvgMs(avg);
        response.setMinMs(min);
        response.setMaxMs(max);
        response.setComparisons(comparisons);
        response.setInterchanges(interchanges);

        return response;
    }

    // ─────────────────────────────────────────────
    // RUN SORT
    // ─────────────────────────────────────────────
    private void runSort(String algo, int[] arr1, EinSehrya einSehrya) {
        SortingAlgorithm sortingAlgorithm = switch (algo) {
            case "bubble"    -> new BubbleSort();
            case "selection" -> new SelectionSort();
            case "insertion" -> new InsertionSort();
            case "merge"     -> new MergeSort();
            case "heap"      -> new HeapSort();
            case "quick"     -> new QuickSort();
            default -> throw new IllegalArgumentException("Unknown algorithm: " + algo);
        };
        sortingAlgorithm.sort(arr1, einSehrya);
    }

    // ─────────────────────────────────────────────
    // GENERATE ARRAY
    // ─────────────────────────────────────────────
    private int[] generateArray(int size, String mode) {
        int[] arr1 = new int[size];
        Random rand = new Random();

        switch (mode) {
            case "random" -> {
                for (int i = 0; i < size; i++)
                    arr1[i] = rand.nextInt(10000);
            }
            case "sorted" -> {
                for (int i = 0; i < size; i++)
                    arr1[i] = i + 1;
            }
            case "inversely_sorted" -> {
                for (int i = 0; i < size; i++)
                    arr1[i] = size - i;
            }
            default -> throw new IllegalArgumentException("Unknown mode: " + mode);
        }
        return arr1;
    }

    // ─────────────────────────────────────────────
    // PARSE FILE CONTENT
    // format: arr=[1,2,8,4,9,77]
    // ─────────────────────────────────────────────
    private int[] parseFileContent(String content) {
        // Extract what is inside the brackets
        int start = content.indexOf('[');
        int end   = content.indexOf(']');
        String numbers = content.substring(start + 1, end);

        String[] parts = numbers.split(",");
        int[] arr1 = new int[parts.length];
        for (int i = 0; i < parts.length; i++)
            arr1[i] = Integer.parseInt(parts[i].trim());
        return arr1;
    }
}