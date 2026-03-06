export interface SortStep {
  type: 'compare' | 'swap' | 'overwrite' | 'markSorted' | 'pivot';
  indices: number[];
  values?: number[];
}