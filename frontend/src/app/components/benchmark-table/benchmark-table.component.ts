import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BenchmarkResponse } from '../../models/benchmark.model';

@Component({
  selector: 'app-benchmark-table',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './benchmark-table.component.html',
  styleUrls: ['./benchmark-table.component.css']
})
export class BenchmarkTableComponent {

  @Input() results: BenchmarkResponse[] = [];
  @Input() inputArrays: { name: string; values: number[] }[] = [];
  @Output() cleared = new EventEmitter<void>();

  sortCol: keyof BenchmarkResponse = 'avgMs';
  sortAsc = true;
  showArrayPanel = true;
  exportSuccess = false;
  showArrayColumn = true;

  get fastest(): BenchmarkResponse | null {
    if (!this.results.length) return null;
    return this.results.reduce((a, b) => a.avgMs < b.avgMs ? a : b);
  }

  get sortedResults(): BenchmarkResponse[] {
    return [...this.results].sort((a, b) => {
      const av = a[this.sortCol];
      const bv = b[this.sortCol];
      if (typeof av === 'string' && typeof bv === 'string')
        return this.sortAsc ? av.localeCompare(bv) : bv.localeCompare(av);
      return this.sortAsc
        ? (av as number) - (bv as number)
        : (bv as number) - (av as number);
    });
  }

  sort(col: keyof BenchmarkResponse) {
    this.sortCol === col
      ? (this.sortAsc = !this.sortAsc)
      : (this.sortCol = col, this.sortAsc = true);
  }

  getSortIcon(col: string): string {
    if (this.sortCol !== col) return '↕';
    return this.sortAsc ? '↑' : '↓';
  }

  formatAlgo(algo: string): string {
    const map: Record<string, string> = {
      bubble: 'Bubble Sort', selection: 'Selection Sort', insertion: 'Insertion Sort',
      merge: 'Merge Sort', heap: 'Heap Sort', quick: 'Quick Sort'
    };
    return map[algo] ?? algo;
  }

  previewArray(arr: number[]): string {
    if (!arr || arr.length === 0) return '—';
    const preview = arr.slice(0, 8).join(', ');
    return arr.length > 8 ? `[${preview}, ...]` : `[${preview}]`;
  }

  exportCSV() {
    const headers = [
      'Algorithm', 'Array Size', 'Mode / File', 'Runs',
      'Avg ms', 'Min ms', 'Max ms', 'Comparisons', 'Interchanges',
      'Original Array'
    ];

    const rows = this.sortedResults.map(r => [
      this.formatAlgo(r.algorithm),
      r.arraySize,
      r.generationMode,
      r.runs,
      r.avgMs.toFixed(6),
      r.minMs.toFixed(6),
      r.maxMs.toFixed(6),
      r.comparisons,
      r.interchanges,
      r.originalArray && r.originalArray.length
        ? `"[${r.originalArray.join(',')}]"`
        : '""'
    ]);

    let csv = headers.join(',') + '\n';
    rows.forEach(r => { csv += r.join(',') + '\n'; });

    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const url  = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href     = url;
    link.download = `sortlab_${Date.now()}.csv`;
    link.click();
    URL.revokeObjectURL(url);

    this.exportSuccess = true;
    setTimeout(() => this.exportSuccess = false, 2500);
  }
}