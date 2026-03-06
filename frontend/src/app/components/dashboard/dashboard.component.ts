import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { VisualizationRequest, VisualizationResponse } from '../../models/visualization.model';
import { BenchmarkRequest, BenchmarkResponse } from '../../models/benchmark.model';
import { VisualizerComponent } from '../visualizer/visualizer.component';
import { BenchmarkTableComponent } from '../benchmark-table/benchmark-table.component';

export interface ParsedArray {
  id: string; name: string; values: number[]; color: string; selected: boolean;
}
export interface VizInstance {
  label: string; color: string; response: VisualizationResponse;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, VisualizerComponent, BenchmarkTableComponent],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent {

  mode: 'visualization' | 'benchmark' = 'visualization';
  viewMode: 'single' | 'stacked' | 'compare' = 'single';

  inputType: 'generate' | 'file' = 'generate';
  genMode = 'random';
  selectedAlgo = 'merge';
  selectedAlgos: string[] = ['bubble','selection','insertion','merge','heap','quick'];
  arraySize = 35;
  sizeAuto = false;
  runs = 10;
  fileName = '';
  fileContent = '';
  loading = false;
  errorMsg = '';

  displayComparisons = 0;
  displayInterchanges = 0;
  displaySteps = 0;

  vizInstances: VizInstance[] = [];
  benchResults: BenchmarkResponse[] = [];
  benchmarkInputArrays: { name: string; values: number[] }[] = [];

  managerOpen = false;
  parsedArrays: ParsedArray[] = [];
  arrayErr = '';

  private colors = ['#1A05A2','#DE1A58','#F67D31','#8F0177','#2ed573','#4a90d9','#a29bfe','#fd79a8'];

  allAlgos = [
    {value:'bubble',label:'Bubble Sort'},{value:'selection',label:'Selection Sort'},
    {value:'insertion',label:'Insertion Sort'},{value:'merge',label:'Merge Sort'},
    {value:'heap',label:'Heap Sort'},{value:'quick',label:'Quick Sort'},
  ];

  constructor(private api: ApiService) {}

  setMode(m: 'visualization' | 'benchmark') {
    this.killAll();
    this.mode = m;
    this.errorMsg = '';
  }

  private killAll() {
    this.vizInstances = [];
    this.benchResults = [];
    this.benchmarkInputArrays = [];
    this.viewMode = 'single';
    this.displayComparisons = 0;
    this.displayInterchanges = 0;
    this.displaySteps = 0;
  }

  toggleAlgo(val: string, e: Event) {
    const checked = (e.target as HTMLInputElement).checked;
    this.selectedAlgos = checked ? [...this.selectedAlgos, val] : this.selectedAlgos.filter(a => a !== val);
  }
  selectAll() { this.selectedAlgos = this.allAlgos.map(a => a.value); }
  clearAll()  { this.selectedAlgos = []; }

  onFile(e: Event) {
    const file = (e.target as HTMLInputElement).files?.[0];
    if (!file) return;
    this.fileName = file.name;
    const reader = new FileReader();
    reader.onload = (ev) => { this.fileContent = ev.target?.result as string; };
    reader.readAsText(file);
  }

  get selectedArrays(): ParsedArray[] { return this.parsedArrays.filter(a => a.selected); }
  selectAllArrays() { this.parsedArrays.forEach(a => a.selected = true); }
  clearAllArrays()  { this.parsedArrays.forEach(a => a.selected = false); }
  removeArray(id: string) { this.parsedArrays = this.parsedArrays.filter(a => a.id !== id); }

  onMultiFiles(e: Event) {
    const files = Array.from((e.target as HTMLInputElement).files || []);
    this.arrayErr = '';
    files.forEach(f => this.parseFile(f));
    (e.target as HTMLInputElement).value = '';
  }

  private parseFile(file: File) {
    const reader = new FileReader();
    reader.onload = (ev) => {
      try {
        const content = ev.target?.result as string;
        const values = this.parseContent(content);
        if (!values.length) { this.arrayErr = `${file.name}: no valid numbers`; return; }
        const id = Date.now().toString() + Math.random().toString(36).slice(2);
        this.parsedArrays = [...this.parsedArrays, {
          id, name: file.name.replace('.txt',''), values,
          color: this.colors[this.parsedArrays.length % this.colors.length],
          selected: true
        }];
      } catch { this.arrayErr = `${file.name}: parse error`; }
    };
    reader.readAsText(file);
  }

  parseContent(content: string): number[] {
    const t = content.trim();
    const nums = t.includes('[') ? t.substring(t.indexOf('[') + 1, t.indexOf(']')) : t;
    return nums.split(',').map(s => parseInt(s.trim(), 10)).filter(n => !isNaN(n));
  }

  run() {
    this.errorMsg = '';
    this.killAll();
    this.mode === 'visualization' ? this.runViz() : this.runBench();
  }

  private runViz() {
    if (this.inputType === 'generate' && !this.sizeAuto && (this.arraySize < 2 || this.arraySize > 100)) {
      this.errorMsg = 'Size must be 2–100.'; return;
    }

    // If upload mode, use array manager files
    if (this.inputType === 'file') {
      if (this.selectedArrays.length > 0) {
        this.loadArraysForViz(this.selectedArrays, 'stacked');
        return;
      } else {
        this.errorMsg = 'No file uploaded. Add files via Array Manager.'; return;
      }
    }

    const req: VisualizationRequest = {
      algorithm: this.selectedAlgo,
      inputType: this.inputType,
      generationMode: this.genMode,
      size: this.sizeAuto ? 0 : this.arraySize,
    };

    this.loading = true;
    this.api.visualize(req).subscribe({
      next: (res) => {
        this.vizInstances = [{ label: 'Generated Array', color: '#1A05A2', response: res }];
        this.viewMode = 'single';
        this.displayComparisons = res.totalComparisons;
        this.displayInterchanges = res.totalInterchanges;
        this.displaySteps = res.steps.length;
        this.loading = false;
      },
      error: (err) => {
        this.errorMsg = typeof err.error === 'string' ? err.error : 'Backend error. Is Spring Boot running?';
        this.loading = false;
      }
    });
  }

  private runBench() {
    if (!this.selectedAlgos.length) { this.errorMsg = 'Select at least one algorithm.'; return; }
    if (this.runs < 1 || this.runs > 100) { this.errorMsg = 'Runs must be 1–100.'; return; }

    // If upload mode, use array manager files
    if (this.inputType === 'file') {
      if (this.selectedArrays.length > 0) {
        this.onBenchmarkArrays();
        return;
      } else {
        this.errorMsg = 'No file uploaded. Add files via Array Manager.'; return;
      }
    }

    this.loading = true;

    // Generate mode — silently fetch array for display
    const vizReq: VisualizationRequest = {
      algorithm: this.selectedAlgos[0],
      inputType: 'generate',
      generationMode: this.genMode,
      size: this.sizeAuto ? 0 : this.arraySize,
    };

    this.api.visualize(vizReq).subscribe({
      next: (res) => {
        this.benchmarkInputArrays = [{ name: 'Generated Array', values: res.originalArray }];
        this.doGenerateBench();
      },
      error: () => {
        this.benchmarkInputArrays = [];
        this.doGenerateBench();
      }
    });
  }

  private doGenerateBench() {
    const req: BenchmarkRequest = {
      algorithms: this.selectedAlgos,
      inputType: 'generate',
      generationMode: this.genMode,
      runs: this.runs,
      size: this.sizeAuto ? 0 : this.arraySize,
    };
    this.api.benchmark(req).subscribe({
      next: (res) => { this.benchResults = res; this.loading = false; },
      error: (err) => {
        this.errorMsg = typeof err.error === 'string' ? err.error : 'Backend error. Is Spring Boot running?';
        this.loading = false;
      }
    });
  }

  private loadArraysForViz(arrays: ParsedArray[], mode: 'stacked' | 'compare') {
    this.killAll();
    this.loading = true;
    let done = 0;
    const results: VizInstance[] = new Array(arrays.length);

    arrays.forEach((arr, i) => {
      const req: VisualizationRequest = {
        algorithm: this.selectedAlgo,
        inputType: 'file',
        fileContent: `arr=[${arr.values.join(',')}]`,
      };
      this.api.visualize(req).subscribe({
        next: (res) => {
          results[i] = { label: arr.name, color: arr.color, response: res };
          done++;
          if (done === arrays.length) {
            this.vizInstances = results.filter(r => !!r);
            this.viewMode = mode;
            if (this.vizInstances[0]) {
              this.displayComparisons = this.vizInstances[0].response.totalComparisons;
              this.displayInterchanges = this.vizInstances[0].response.totalInterchanges;
              this.displaySteps = this.vizInstances[0].response.steps.length;
            }
            this.loading = false;
          }
        },
        error: () => { done++; if (done === arrays.length) this.loading = false; }
      });
    });
  }

  onVisualizeArrays() {
    const arrays = this.selectedArrays;
    if (!arrays.length) return;
    this.loadArraysForViz(arrays, 'stacked');
  }

  onCompareArrays() {
    const arrays = this.selectedArrays;
    if (arrays.length < 2) return;
    this.loadArraysForViz(arrays, 'compare');
  }

  onBenchmarkArrays() {
    const arrays = this.selectedArrays;
    if (!arrays.length || !this.selectedAlgos.length) return;

    this.killAll();
    this.loading = true;

    this.benchmarkInputArrays = arrays.map(a => ({ name: a.name, values: a.values }));

    let done = 0;
    const allResults: BenchmarkResponse[] = [];

    arrays.forEach(arr => {
      const req: BenchmarkRequest = {
        algorithms: this.selectedAlgos,
        inputType: 'file',
        fileContent: `arr=[${arr.values.join(',')}]`,
        fileName: arr.name,
        runs: this.runs,
        size: arr.values.length,
      };
      this.api.benchmark(req).subscribe({
        next: (res) => {
          allResults.push(...res);
          done++;
          if (done === arrays.length) {
            this.benchResults = allResults;
            this.loading = false;
          }
        },
        error: (err) => {
          done++;
          if (done === arrays.length) {
            this.errorMsg = typeof err.error === 'string' ? err.error : 'Backend error. Is Spring Boot running?';
            this.loading = false;
          }
        }
      });
    });
  }
}