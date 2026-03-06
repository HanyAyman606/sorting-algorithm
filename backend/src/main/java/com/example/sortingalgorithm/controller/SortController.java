package com.example.sortingalgorithm.controller;

import com.example.sortingalgorithm.dto.BenchmarkRequest;
import com.example.sortingalgorithm.dto.BenchmarkResponse;
import com.example.sortingalgorithm.dto.VisualizationRequest;
import com.example.sortingalgorithm.dto.VisualizationResponse;
import com.example.sortingalgorithm.service.SortingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/sort")
public class SortController {

   
    private static final Set<String> VALID_ALGORITHMS = Set.of(
            "bubble", "selection", "insertion", "merge", "heap", "quick"
    );
    private static final Set<String> VALID_INPUT_TYPES = Set.of(
            "generate", "file"
    );
    private static final Set<String> VALID_GENERATION_MODES = Set.of(
            "random", "sorted", "inversely_sorted"
    );

    private static final int MAX_VISUALIZATION_SIZE = 100;
    private static final int MAX_BENCHMARK_SIZE     = 10000;
    private static final int MAX_RUNS               = 100;

 
    private final SortingService sortingService;

    public SortController(SortingService sortingService) {
        this.sortingService = sortingService;
    }

    @PostMapping("/visualize")
    public ResponseEntity<?> visualize(@RequestBody VisualizationRequest request) {

        if (request.getInputType() == null || request.getInputType().isBlank()) {
            return ResponseEntity.badRequest().body("inputType is required ('generate' or 'file').");
        }
        if (!VALID_INPUT_TYPES.contains(request.getInputType())) {
            return ResponseEntity.badRequest().body("inputType must be 'generate' or 'file'.");
        }

        if (request.getAlgorithm() == null || request.getAlgorithm().isBlank()) {
            return ResponseEntity.badRequest().body("Algorithm is required.");
        }
        if (!VALID_ALGORITHMS.contains(request.getAlgorithm())) {
            return ResponseEntity.badRequest().body(
                "Unknown algorithm '" + request.getAlgorithm() + "'. " +
                "Valid: bubble, selection, insertion, merge, heap, quick."
            );
        }

        if (request.getInputType().equals("generate")) {

            if (request.getGenerationMode() == null || request.getGenerationMode().isBlank()) {
                return ResponseEntity.badRequest().body("generationMode is required when inputType is 'generate'.");
            }
            if (!VALID_GENERATION_MODES.contains(request.getGenerationMode())) {
                return ResponseEntity.badRequest().body(
                    "generationMode must be 'random', 'sorted', or 'inversely_sorted'."
                );
            }

            if (request.getSize() < 0) {
                return ResponseEntity.badRequest().body("Array size cannot be negative.");
            }
            if (request.getSize() > MAX_VISUALIZATION_SIZE) {
                return ResponseEntity.badRequest().body(
                    "Array size must be " + MAX_VISUALIZATION_SIZE + " or less for visualization."
                );
            }

        } else {
            if (request.getFileContent() == null || request.getFileContent().isBlank()) {
                return ResponseEntity.badRequest().body("fileContent is required when inputType is 'file'.");
            }
        }

        VisualizationResponse response = sortingService.runVisualization(request);
        return ResponseEntity.ok(response);
    }

   
    @PostMapping("/benchmark")
    public ResponseEntity<?> benchmark(@RequestBody BenchmarkRequest request) {

        if (request.getAlgorithms() == null || request.getAlgorithms().isEmpty()) {
            return ResponseEntity.badRequest().body("Select at least one algorithm.");
        }
        for (String algo : request.getAlgorithms()) {
            if (!VALID_ALGORITHMS.contains(algo)) {
                return ResponseEntity.badRequest().body(
                    "Unknown algorithm '" + algo + "'. " +
                    "Valid: bubble, selection, insertion, merge, heap, quick."
                );
            }
        }

        if (request.getInputType() == null || request.getInputType().isBlank()) {
            return ResponseEntity.badRequest().body("inputType is required ('generate' or 'file').");
        }
        if (!VALID_INPUT_TYPES.contains(request.getInputType())) {
            return ResponseEntity.badRequest().body("inputType must be 'generate' or 'file'.");
        }

        if (request.getInputType().equals("generate")) {

            if (request.getGenerationMode() == null || request.getGenerationMode().isBlank()) {
                return ResponseEntity.badRequest().body("generationMode is required when inputType is 'generate'.");
            }
            if (!VALID_GENERATION_MODES.contains(request.getGenerationMode())) {
                return ResponseEntity.badRequest().body(
                    "generationMode must be 'random', 'sorted', or 'inversely_sorted'."
                );
            }

            if (request.getSize() < 0) {
                return ResponseEntity.badRequest().body("Array size cannot be negative.");
            }
            if (request.getSize() > MAX_BENCHMARK_SIZE) {
                return ResponseEntity.badRequest().body(
                    "Array size must be " + MAX_BENCHMARK_SIZE + " or less for benchmark."
                );
            }

        } else {
            
            if (request.getFileContent() == null || request.getFileContent().isBlank()) {
                return ResponseEntity.badRequest().body("fileContent is required when inputType is 'file'.");
            }
            if (request.getFileName() == null || request.getFileName().isBlank()) {
                return ResponseEntity.badRequest().body("fileName is required when inputType is 'file'.");
            }
        }

        if (request.getRuns() <= 0) {
            return ResponseEntity.badRequest().body("Number of runs must be at least 1.");
        }
        if (request.getRuns() > MAX_RUNS) {
            return ResponseEntity.badRequest().body("Number of runs cannot exceed " + MAX_RUNS + ".");
        }

        List<BenchmarkResponse> response = sortingService.runBenchmark(request);
        return ResponseEntity.ok(response);
    }
}