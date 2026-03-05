package com.example.sortingalgorithm.dto;

import java.util.List;

public class BenchmarkRequest {
    private List<String> algorithms;  // list from checkboxes
    private String inputType;
    private String generationMode;
    private int size;
    private int runs;
    private String fileContent;       // single file content
    private String fileName;          // single file name

    public List<String> getAlgorithms() { return algorithms; }
    public void setAlgorithms(List<String> algorithms) { this.algorithms = algorithms; }

    public String getInputType() { return inputType; }
    public void setInputType(String inputType) { this.inputType = inputType; }

    public String getGenerationMode() { return generationMode; }
    public void setGenerationMode(String generationMode) { this.generationMode = generationMode; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public int getRuns() { return runs; }
    public void setRuns(int runs) { this.runs = runs; }

    public String getFileContent() { return fileContent; }
    public void setFileContent(String fileContent) { this.fileContent = fileContent; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
}