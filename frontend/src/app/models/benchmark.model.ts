export interface BenchmarkRequest {
  algorithms: string[];
  inputType: 'generate' | 'file';
  generationMode?: string;
  size: number;          
  runs: number;
  fileContent?: string;
  fileName?: string;
}

export interface BenchmarkResponse {
  algorithm: string;
  arraySize: number;
  generationMode: string;
  runs: number;
  avgMs: number;
  minMs: number;
  maxMs: number;
  comparisons: number;
  interchanges: number;
}