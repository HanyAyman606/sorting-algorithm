package com.example.sortingalgorithm.dto;

public class BenchmarkResponse {
    private String algorithm;
    private int arraySize;
    private String generationMode;
    private int runs;
    private double avgMs;
    private double minMs;
    private double maxMs;
    private long comparisons;
    private long interchanges;

    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }

    public int getArraySize() { return arraySize; }
    public void setArraySize(int arraySize) { this.arraySize = arraySize; }

    public String getGenerationMode() { return generationMode; }
    public void setGenerationMode(String generationMode) { this.generationMode = generationMode; }

    public int getRuns() { return runs; }
    public void setRuns(int runs) { this.runs = runs; }

    public double getAvgMs() { return avgMs; }
    public void setAvgMs(double avgMs) { this.avgMs = avgMs; }

    public double getMinMs() { return minMs; }
    public void setMinMs(double minMs) { this.minMs = minMs; }

    public double getMaxMs() { return maxMs; }
    public void setMaxMs(double maxMs) { this.maxMs = maxMs; }

    public long getComparisons() { return comparisons; }
    public void setComparisons(long comparisons) { this.comparisons = comparisons; }

    public long getInterchanges() { return interchanges; }
    public void setInterchanges(long interchanges) { this.interchanges = interchanges; }
}