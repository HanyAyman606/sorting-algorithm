package com.example.sortingalgorithm.dto;

import java.util.List;

public class VisualizationResponse {
    private List<SortStep> steps;
    private long totalComparisons;
    private long totalInterchanges;
    private int[] originalArray;

    public List<SortStep> getSteps() { return steps; }
    public void setSteps(List<SortStep> steps) { this.steps = steps; }

    public long getTotalComparisons() { return totalComparisons; }
    public void setTotalComparisons(long totalComparisons) { this.totalComparisons = totalComparisons; }

    public long getTotalInterchanges() { return totalInterchanges; }
    public void setTotalInterchanges(long totalInterchanges) { this.totalInterchanges = totalInterchanges; }

    public int[] getOriginalArray() { return originalArray; }
    public void setOriginalArray(int[] originalArray) { this.originalArray = originalArray; }
}