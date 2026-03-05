package com.example.sortingalgorithm.dto;

import java.util.List;

public class SortStep {
    private String type;
    private List<Integer> indices;
    private List<Integer> values;

    public SortStep(String type, List<Integer> indices) {
        this.type = type;
        this.indices = indices;
        this.values = null;
    }

    public SortStep(String type, List<Integer> indices, List<Integer> values) {
        this.type = type;
        this.indices = indices;
        this.values = values;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public List<Integer> getIndices() { return indices; }
    public void setIndices(List<Integer> indices) { this.indices = indices; }

    public List<Integer> getValues() { return values; }
    public void setValues(List<Integer> values) { this.values = values; }
}