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
    // Valid values — used for validation below
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
    private static final int MAX_BENCHMARK_SIZE     = 10_000;
    private static final int MAX_RUNS               = 100;

    // ─────────────────────────────────────────────
    // Service — injected via constructor (best practice)
    // ─────────────────────────────────────────────
    private final SortingService sortingService;

    public SortController(SortingService sortingService) {
        this.sortingService = sortingService;
    }

    // ═════════════════════════════════════════════
    // POST /api/sort/visualize
    // Frontend sends: VisualizationRequest (JSON envelope)
    // Controller: validates → hands to Service
    // Service: does all the work, returns VisualizationResponse
    // ═════════════════════════════════════════════
    @PostMapping("/visualize")
    public ResponseEntity<?> visualize(@RequestBody VisualizationRequest request) {

        // ── 1. inputType must exist and be valid ──
        if (request.getInputType() == null || request.getInputType().isBlank()) {
            return ResponseEntity.badRequest().body("inputType is required (generate or file).");
        }
        if (!VALID_INPUT_TYPES.contains(request.getInputType())) {
            return ResponseEntity.badRequest().body("inputType must be 'generate' or 'file'.");
        }

        // ── 2. algorithm must exist and be one of the 6 ──
        if (request.getAlgorithm() == null || request.getAlgorithm().isBlank()) {
            return ResponseEntity.badRequest().body("Algorithm is required.");
        }
        if (!VALID_ALGORITHMS.contains(request.getAlgorithm())) {
            return ResponseEntity.badRequest().body(
                "Unknown algorithm '" + request.getAlgorithm() + "'. " +
                "Valid options: bubble, selection, insertion, merge, heap, quick."
            );
        }

        // ── 3. Validate based on inputType ──
        if (request.getInputType().equals("generate")) {

            // generationMode is required for generate mode
            if (request.getGenerationMode() == null || request.getGenerationMode().isBlank()) {
                return ResponseEntity.badRequest().body("generationMode is required when inputType is 'generate'.");
            }
            if (!VALID_GENERATION_MODES.contains(request.getGenerationMode())) {
                return ResponseEntity.badRequest().body(
                    "generationMode must be 'random', 'sorted', or 'inversely_sorted'."
                );
            }

            // size must be > 0 and <= 100 for visualization
            if (request.getSize() <= 0) {
                return ResponseEntity.badRequest().body("Array size must be greater than 0.");
            }
            if (request.getSize() > MAX_VISUALIZATION_SIZE) {
                return ResponseEntity.badRequest().body(
                    "Array size must be " + MAX_VISUALIZATION_SIZE + " or less for visualization."
                );
            }

        } else {
            // inputType = "file" — fileContent must be present
            if (request.getFileContent() == null || request.getFileContent().isBlank()) {
                return ResponseEntity.badRequest().body("fileContent is required when inputType is 'file'.");
            }
        }

        // ── 4. All good → delegate to Service ──
        VisualizationResponse response = sortingService.runVisualization(request);
        return ResponseEntity.ok(response);
    }

    // ═════════════════════════════════════════════
    // POST /api/sort/benchmark
    // Frontend sends: BenchmarkRequest (JSON envelope)
    // Controller: validates → hands to Service
    // Service: does all the work, returns List<BenchmarkResponse>
    // ═════════════════════════════════════════════
    @PostMapping("/benchmark")
    public ResponseEntity<?> benchmark(@RequestBody BenchmarkRequest request) {

        // ── 1. algorithms list must exist and not be empty ──
        if (request.getAlgorithms() == null || request.getAlgorithms().isEmpty()) {
            return ResponseEntity.badRequest().body("Select at least one algorithm.");
        }

        // each algorithm in the list must be valid
        for (String algo : request.getAlgorithms()) {
            if (!VALID_ALGORITHMS.contains(algo)) {
                return ResponseEntity.badRequest().body(
                    "Unknown algorithm '" + algo + "'. " +
                    "Valid options: bubble, selection, insertion, merge, heap, quick."
                );
            }
        }

        // ── 2. inputType must exist and be valid ──
        if (request.getInputType() == null || request.getInputType().isBlank()) {
            return ResponseEntity.badRequest().body("inputType is required (generate or file).");
        }
        if (!VALID_INPUT_TYPES.contains(request.getInputType())) {
            return ResponseEntity.badRequest().body("inputType must be 'generate' or 'file'.");
        }

        // ── 3. Validate based on inputType ──
        if (request.getInputType().equals("generate")) {

            // generationMode is required
            if (request.getGenerationMode() == null || request.getGenerationMode().isBlank()) {
                return ResponseEntity.badRequest().body("generationMode is required when inputType is 'generate'.");
            }
            if (!VALID_GENERATION_MODES.contains(request.getGenerationMode())) {
                return ResponseEntity.badRequest().body(
                    "generationMode must be 'random', 'sorted', or 'inversely_sorted'."
                );
            }

            // size must be > 0 and <= 10,000 for benchmark
            if (request.getSize() <= 0) {
                return ResponseEntity.badRequest().body("Array size must be greater than 0.");
            }
            if (request.getSize() > MAX_BENCHMARK_SIZE) {
                return ResponseEntity.badRequest().body(
                    "Array size must be " + MAX_BENCHMARK_SIZE + " or less for benchmark."
                );
            }

        } else {
            // inputType = "file" — fileContent and fileName must be present
            if (request.getFileContent() == null || request.getFileContent().isBlank()) {
                return ResponseEntity.badRequest().body("fileContent is required when inputType is 'file'.");
            }
            if (request.getFileName() == null || request.getFileName().isBlank()) {
                return ResponseEntity.badRequest().body("fileName is required when inputType is 'file'.");
            }
        }

        // ── 4. runs must be between 1 and MAX_RUNS ──
        if (request.getRuns() <= 0) {
            return ResponseEntity.badRequest().body("Number of runs must be at least 1.");
        }
        if (request.getRuns() > MAX_RUNS) {
            return ResponseEntity.badRequest().body("Number of runs cannot exceed " + MAX_RUNS + ".");
        }

        // ── 5. All good → delegate to Service ──
        List<BenchmarkResponse> response = sortingService.runBenchmark(request);
        return ResponseEntity.ok(response);
    }
}