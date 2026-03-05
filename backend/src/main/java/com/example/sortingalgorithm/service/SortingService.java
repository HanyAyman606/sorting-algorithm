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
    // Auto-size defaults
    //   size=0 from frontend means "auto"
    //   Visualization auto → 50 elements (safe for animation)
    //   Benchmark auto     → 1000 elements (meaningful timing)
    // ─────────────────────────────────────────────
    private static final int AUTO_SIZE_VISUALIZATION = 50;
    private static final int AUTO_SIZE_BENCHMARK     = 1000;

    // ─────────────────────────────────────────────
    // VISUALIZATION
    // ─────────────────────────────────────────────
    public VisualizationResponse runVisualization(VisualizationRequest request) {

        // Step 1: Resolve array
        int[] arr1;
        if (request.getInputType().equals("file")) {
            arr1 = parseFileContent(request.getFileContent());
        } else {
            // size=0 → auto default
            int size = (request.getSize() == 0) ? AUTO_SIZE_VISUALIZATION : request.getSize();
            arr1 = generateArray(size, request.getGenerationMode());
        }

        // Step 2: Save original for frontend to render initial bars
        int[] originalArray = Arrays.copyOf(arr1, arr1.length);

        // Step 3: Visualization tracker — records every step
        VisualizationTracker tracker = new VisualizationTracker();

        // Step 4: Run the sort
        runSort(request.getAlgorithm(), arr1, tracker);

        // Step 5: Build and return response
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

        // Resolve array — file OR generated
        int[] originalArray;
        String modeName;

        if (request.getInputType().equals("file")) {
            originalArray = parseFileContent(request.getFileContent());
            modeName = request.getFileName();
        } else {
            // size=0 → auto default
            int size = (request.getSize() == 0) ? AUTO_SIZE_BENCHMARK : request.getSize();
            originalArray = generateArray(size, request.getGenerationMode());
            modeName = request.getGenerationMode();
        }

        // Run each selected algorithm (from checkboxes)
        for (String algo : request.getAlgorithms()) {
            BenchmarkResponse res = runSingleBenchmark(
                algo, originalArray, request.getRuns(), modeName
            );
            results.add(res);
        }

        return results;
    }

    // ─────────────────────────────────────────────
    // SINGLE BENCHMARK RUN
    //   - Runs the algorithm `runs` times on fresh copies
    //   - Measures time with nanoTime for precision
    //   - Comparisons/interchanges taken from run #1
    //     (deterministic — same data always gives same count)
    // ─────────────────────────────────────────────
    private BenchmarkResponse runSingleBenchmark(
        String algo, int[] originalArray, int runs, String modeName
    ) {
        double[] times = new double[runs];
        long comparisons  = 0;
        long interchanges = 0;

        for (int i = 0; i < runs; i++) {
            // Fresh copy each run — never sort already-sorted data
            int[] arr1 = Arrays.copyOf(originalArray, originalArray.length);
            BenchmarkTracker tracker = new BenchmarkTracker();

            long start = System.nanoTime();
            runSort(algo, arr1, tracker);
            long end = System.nanoTime();

            times[i] = (end - start) / 1_000_000.0; // ns → ms

            // Capture counts from first run only
            if (i == 0) {
                comparisons  = tracker.getComparisons();
                interchanges = tracker.getInterchanges();
            }
        }

        // Compute avg / min / max
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
    // RUN SORT — picks the right algorithm object
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
    //   random          → integers 0–9999
    //   sorted          → 1, 2, 3 ... size
    //   inversely_sorted → size, size-1 ... 1
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
    //
    // Supports two formats from .txt files:
    //   Format A (bracket):  arr=[1,2,8,4,9,77]
    //   Format B (plain):    1,2,8,4,9,77
    //
    // Frontend reads the file as raw string via FileReader API
    // and sends it as fileContent in the request body.
    // ─────────────────────────────────────────────
    private int[] parseFileContent(String content) {
        String trimmed = content.trim();
        String numbers;

        if (trimmed.contains("[") && trimmed.contains("]")) {
            // Format A: arr=[1,2,3] — extract inside brackets
            int start = trimmed.indexOf('[');
            int end   = trimmed.indexOf(']');
            numbers = trimmed.substring(start + 1, end);
        } else {
            // Format B: plain comma-separated — use as-is
            numbers = trimmed;
        }

        String[] parts = numbers.split(",");
        int[] arr1 = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            arr1[i] = Integer.parseInt(parts[i].trim());
        }
        return arr1;
    }
}