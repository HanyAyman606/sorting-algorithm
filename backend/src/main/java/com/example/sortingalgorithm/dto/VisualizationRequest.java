package com.example.sortingalgorithm.dto;

public class VisualizationRequest {
    private String algorithm;
    private String inputType;
    private String generationMode;
    private int size;
    private String fileContent;

    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }

    public String getInputType() { return inputType; }
    public void setInputType(String inputType) { this.inputType = inputType; }

    public String getGenerationMode() { return generationMode; }
    public void setGenerationMode(String generationMode) { this.generationMode = generationMode; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public String getFileContent() { return fileContent; }
    public void setFileContent(String fileContent) { this.fileContent = fileContent; }
}