import { SortStep } from './sort-step.model';

export interface VisualizationRequest {
  algorithm: string;
  inputType: 'generate' | 'file';
  generationMode?: string;
  size?: number;          
  fileContent?: string;
}

export interface VisualizationResponse {
  steps: SortStep[];
  totalComparisons: number;
  totalInterchanges: number;
  originalArray: number[];
}