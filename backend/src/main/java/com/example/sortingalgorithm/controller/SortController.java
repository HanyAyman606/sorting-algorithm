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

    // ─────────────────────────────────────────────
    // Constants — single source of truth for limits
    // ─────────────────────────────────────────────
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

    // ─────────────────────────────────────────────
    // Constructor injection — best practice
    // ─────────────────────────────────────────────
    private final SortingService sortingService;

    public SortController(SortingService sortingService) {
        this.sortingService = sortingService;
    }

    // ═════════════════════════════════════════════
    // POST /api/sort/visualize
    //
    // Flow:
    //   Frontend sends VisualizationRequest (JSON)
    //   Controller validates fields
    //   Controller delegates to SortingService
    //   Returns VisualizationResponse (steps + stats + originalArray)
    // ═════════════════════════════════════════════
    @PostMapping("/visualize")
    public ResponseEntity<?> visualize(@RequestBody VisualizationRequest request) {

        // ── 1. inputType ──
        if (request.getInputType() == null || request.getInputType().isBlank()) {
            return ResponseEntity.badRequest().body("inputType is required ('generate' or 'file').");
        }
        if (!VALID_INPUT_TYPES.contains(request.getInputType())) {
            return ResponseEntity.badRequest().body("inputType must be 'generate' or 'file'.");
        }

        // ── 2. algorithm (single for visualization) ──
        if (request.getAlgorithm() == null || request.getAlgorithm().isBlank()) {
            return ResponseEntity.badRequest().body("Algorithm is required.");
        }
        if (!VALID_ALGORITHMS.contains(request.getAlgorithm())) {
            return ResponseEntity.badRequest().body(
                "Unknown algorithm '" + request.getAlgorithm() + "'. " +
                "Valid: bubble, selection, insertion, merge, heap, quick."
            );
        }

        // ── 3. inputType-specific validation ──
        if (request.getInputType().equals("generate")) {

            if (request.getGenerationMode() == null || request.getGenerationMode().isBlank()) {
                return ResponseEntity.badRequest().body("generationMode is required when inputType is 'generate'.");
            }
            if (!VALID_GENERATION_MODES.contains(request.getGenerationMode())) {
                return ResponseEntity.badRequest().body(
                    "generationMode must be 'random', 'sorted', or 'inversely_sorted'."
                );
            }

            // size=0 means auto → service defaults to 50 for visualization
            if (request.getSize() < 0) {
                return ResponseEntity.badRequest().body("Array size cannot be negative.");
            }
            if (request.getSize() > MAX_VISUALIZATION_SIZE) {
                return ResponseEntity.badRequest().body(
                    "Array size must be " + MAX_VISUALIZATION_SIZE + " or less for visualization."
                );
            }

        } else {
            // file mode
            if (request.getFileContent() == null || request.getFileContent().isBlank()) {
                return ResponseEntity.badRequest().body("fileContent is required when inputType is 'file'.");
            }
        }

        // ── 4. Delegate to Service ──
        VisualizationResponse response = sortingService.runVisualization(request);
        return ResponseEntity.ok(response);
    }

    // ═════════════════════════════════════════════
    // POST /api/sort/benchmark
    //
    // Flow:
    //   Frontend sends BenchmarkRequest (JSON)
    //   algorithms is a List<String> from checkboxes (1 to 6)
    //   size=0 means auto → service defaults to 1000
    //   Controller validates fields
    //   Controller delegates to SortingService
    //   Returns List<BenchmarkResponse> — one row per algorithm
    // ═════════════════════════════════════════════
    @PostMapping("/benchmark")
    public ResponseEntity<?> benchmark(@RequestBody BenchmarkRequest request) {

        // ── 1. algorithms list — from checkboxes ──
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

        // ── 2. inputType ──
        if (request.getInputType() == null || request.getInputType().isBlank()) {
            return ResponseEntity.badRequest().body("inputType is required ('generate' or 'file').");
        }
        if (!VALID_INPUT_TYPES.contains(request.getInputType())) {
            return ResponseEntity.badRequest().body("inputType must be 'generate' or 'file'.");
        }

        // ── 3. inputType-specific validation ──
        if (request.getInputType().equals("generate")) {

            if (request.getGenerationMode() == null || request.getGenerationMode().isBlank()) {
                return ResponseEntity.badRequest().body("generationMode is required when inputType is 'generate'.");
            }
            if (!VALID_GENERATION_MODES.contains(request.getGenerationMode())) {
                return ResponseEntity.badRequest().body(
                    "generationMode must be 'random', 'sorted', or 'inversely_sorted'."
                );
            }

            // size=0 means auto → service defaults to 1000 for benchmark
            if (request.getSize() < 0) {
                return ResponseEntity.badRequest().body("Array size cannot be negative.");
            }
            if (request.getSize() > MAX_BENCHMARK_SIZE) {
                return ResponseEntity.badRequest().body(
                    "Array size must be " + MAX_BENCHMARK_SIZE + " or less for benchmark."
                );
            }

        } else {
            // file mode
            if (request.getFileContent() == null || request.getFileContent().isBlank()) {
                return ResponseEntity.badRequest().body("fileContent is required when inputType is 'file'.");
            }
            if (request.getFileName() == null || request.getFileName().isBlank()) {
                return ResponseEntity.badRequest().body("fileName is required when inputType is 'file'.");
            }
        }

        // ── 4. runs — must be 1..100 ──
        if (request.getRuns() <= 0) {
            return ResponseEntity.badRequest().body("Number of runs must be at least 1.");
        }
        if (request.getRuns() > MAX_RUNS) {
            return ResponseEntity.badRequest().body("Number of runs cannot exceed " + MAX_RUNS + ".");
        }

        // ── 5. Delegate to Service ──
        List<BenchmarkResponse> response = sortingService.runBenchmark(request);
        return ResponseEntity.ok(response);
    }
}