import { Component, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

export interface ParsedArray {
  id: string;
  name: string;
  values: number[];
  color: string;
  selected: boolean;
}

@Component({
  selector: 'app-array-panel',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './array-panel.component.html',
  styleUrls: ['./array-panel.component.css']
})
export class ArrayPanelComponent {

  @Output() arraysSelected = new EventEmitter<ParsedArray[]>();
  @Output() visualizeSelected = new EventEmitter<ParsedArray[]>();
  @Output() compareSelected = new EventEmitter<ParsedArray[]>();

  arrays: ParsedArray[] = [];
  errorMsg = '';

  private colors = [
    '#1A05A2', '#8F0177', '#DE1A58', '#F67D31',
    '#2ed573', '#4a90d9', '#a29bfe', '#fd79a8'
  ];

  onFilesSelected(e: Event) {
    const files = Array.from((e.target as HTMLInputElement).files || []);
    this.errorMsg = '';
    files.forEach(file => this.parseFile(file));
    (e.target as HTMLInputElement).value = '';
  }

  private parseFile(file: File) {
    const reader = new FileReader();
    reader.onload = (ev) => {
      try {
        const content = ev.target?.result as string;
        const values = this.parseContent(content);
        if (values.length === 0) {
          this.errorMsg = `${file.name}: No valid numbers found.`;
          return;
        }
        const id = Date.now().toString() + Math.random().toString(36).slice(2);
        const colorIndex = this.arrays.length % this.colors.length;
        this.arrays = [...this.arrays, {
          id,
          name: file.name,
          values,
          color: this.colors[colorIndex],
          selected: false
        }];
      } catch {
        this.errorMsg = `${file.name}: Could not parse file.`;
      }
    };
    reader.readAsText(file);
  }

  private parseContent(content: string): number[] {
    const trimmed = content.trim();
    let numbers: string;
    if (trimmed.includes('[') && trimmed.includes(']')) {
      numbers = trimmed.substring(trimmed.indexOf('[') + 1, trimmed.indexOf(']'));
    } else {
      numbers = trimmed;
    }
    return numbers.split(',')
      .map(s => parseInt(s.trim(), 10))
      .filter(n => !isNaN(n));
  }

  toggleSelect(arr: ParsedArray) {
    arr.selected = !arr.selected;
  }

  selectAll() { this.arrays.forEach(a => a.selected = true); }
  clearAll()  { this.arrays.forEach(a => a.selected = false); }

  removeArray(id: string) {
    this.arrays = this.arrays.filter(a => a.id !== id);
  }

  get selectedArrays(): ParsedArray[] {
    return this.arrays.filter(a => a.selected);
  }

  onVisualize() {
    const sel = this.selectedArrays;
    if (sel.length === 0) { this.errorMsg = 'Select at least one array.'; return; }
    this.errorMsg = '';
    this.visualizeSelected.emit(sel);
  }

  onCompare() {
    const sel = this.selectedArrays;
    if (sel.length < 2) { this.errorMsg = 'Select at least 2 arrays to compare.'; return; }
    this.errorMsg = '';
    this.compareSelected.emit(sel);
  }
}
