import { Component, Input, OnChanges, OnDestroy, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { VisualizationResponse } from '../../models/visualization.model';
import { SortStep } from '../../models/sort-step.model';

export type BarState = 'default' | 'compare' | 'swap' | 'sorted' | 'pivot';
export interface Bar { value: number; state: BarState; }

@Component({
  selector: 'app-visualizer',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './visualizer.component.html',
  styleUrls: ['./visualizer.component.css']
})
export class VisualizerComponent implements OnChanges, OnDestroy {

  @Input() response!: VisualizationResponse;
  @Input() label: string = 'Array';
  @Input() color: string = '#1A05A2';

  bars: Bar[] = [];
  steps: SortStep[] = [];
  currentStep = 0;
  isPlaying = false;
  speed = 200;
  liveComparisons = 0;
  liveInterchanges = 0;
  soundEnabled = true;
  elapsedMs = 0;

  // Track current pivot index so it stays purple until resolved
  private currentPivotIndex: number = -1;

  private timerInterval: any = null;
  private timerStart = 0;
  private stopFlag = false;
  private maxVal = 1;
  private audioCtx: AudioContext | null = null;

  ngOnChanges(changes: SimpleChanges) {
    if (changes['response'] && this.response) {
      this.hardStop();
      this.initVisualizer();
    }
  }

  ngOnDestroy() {
    this.hardStop();
    this.closeAudio();
  }

  hardStop() {
    this.stopFlag = true;
    this.isPlaying = false;
    this.stopTimer();
    this.closeAudio();
  }

  private closeAudio() {
    if (this.audioCtx) {
      try { this.audioCtx.close(); } catch {}
      this.audioCtx = null;
    }
  }

  private initVisualizer() {
    this.stopFlag = false;
    const arr = [...this.response.originalArray];
    this.maxVal = Math.max(...arr, 1);
    this.steps = this.response.steps;
    this.bars = arr.map(v => ({ value: v, state: 'default' as BarState }));
    this.currentStep = 0;
    this.liveComparisons = 0;
    this.liveInterchanges = 0;
    this.elapsedMs = 0;
    this.currentPivotIndex = -1;
  }

  getBarHeight(value: number): number {
    return Math.max(3, (value / this.maxVal) * 100);
  }

  private startTimer() {
    this.timerStart = Date.now() - this.elapsedMs;
    this.timerInterval = setInterval(() => {
      this.elapsedMs = Date.now() - this.timerStart;
    }, 50);
  }

  private stopTimer() {
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
      this.timerInterval = null;
    }
  }

  get formattedTime(): string {
    if (this.elapsedMs === 0) return '0 ms';
    if (this.elapsedMs < 1000) return `${this.elapsedMs} ms`;
    return `${(this.elapsedMs / 1000).toFixed(2)} s`;
  }

  private getAudioCtx(): AudioContext | null {
    try {
      if (!this.audioCtx || this.audioCtx.state === 'closed') {
        this.audioCtx = new AudioContext();
      }
      return this.audioCtx;
    } catch { return null; }
  }

  private playTone(freq: number, dur: number, type: OscillatorType = 'sine', vol = 0.1) {
    if (!this.soundEnabled) return;
    try {
      const ctx = this.getAudioCtx();
      if (!ctx || ctx.state === 'closed') return;
      const osc  = ctx.createOscillator();
      const gain = ctx.createGain();
      osc.connect(gain);
      gain.connect(ctx.destination);
      osc.type = type;
      osc.frequency.setValueAtTime(freq, ctx.currentTime);
      gain.gain.setValueAtTime(vol, ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + dur);
      osc.start(ctx.currentTime);
      osc.stop(ctx.currentTime + dur);
    } catch {}
  }

  private soundForStep(type: string, value?: number) {
    if (this.stopFlag) return;
    const baseFreq = value ? 200 + (value / this.maxVal) * 600 : 400;
    switch (type) {
      case 'compare':    this.playTone(baseFreq,        0.07, 'sine',     0.09); break;
      case 'swap':       this.playTone(baseFreq * 1.4,  0.10, 'triangle', 0.15); break;
      case 'overwrite':  this.playTone(baseFreq,        0.09, 'triangle', 0.12); break;
      case 'markSorted': this.playTone(880,             0.12, 'sine',     0.10); break;
      case 'pivot':      this.playTone(300,             0.09, 'sawtooth', 0.09); break;
    }
  }

  togglePlay() {
    if (this.isPlaying) {
      this.isPlaying = false;
      this.stopFlag = true;
      this.stopTimer();
    } else {
      this.stopFlag = false;
      this.isPlaying = true;
      this.startTimer();
      this.playLoop();
    }
  }

  private async playLoop() {
    while (this.currentStep < this.steps.length && !this.stopFlag) {
      this.applyStep(this.steps[this.currentStep]);
      this.currentStep++;
      await this.delay(this.speed);
    }
    if (!this.stopFlag && this.currentStep >= this.steps.length) {
      this.stopTimer();
    }
    this.isPlaying = false;
  }

  stepForward() {
    if (this.currentStep < this.steps.length) {
      this.applyStep(this.steps[this.currentStep]);
      this.currentStep++;
    }
  }

  stepBack() {
    if (this.currentStep <= 0) return;
    this.currentStep--;
    this.replayTo(this.currentStep);
  }

  reset() {
    this.hardStop();
    this.stopFlag = false;
    this.bars = [...this.response.originalArray].map(v => ({ value: v, state: 'default' as BarState }));
    this.currentStep = 0;
    this.liveComparisons = 0;
    this.liveInterchanges = 0;
    this.elapsedMs = 0;
    this.currentPivotIndex = -1;
  }

  skipToEnd() {
    this.hardStop();
    this.stopFlag = false;
    while (this.currentStep < this.steps.length) {
      this.applyStepSilent(this.steps[this.currentStep]);
      this.currentStep++;
    }
    this.liveComparisons  = this.response.totalComparisons;
    this.liveInterchanges = this.response.totalInterchanges;
  }

  onSpeedChange(e: Event) {
    this.speed = 1010 - +(e.target as HTMLInputElement).value;
  }

  private applyStep(step: SortStep) {
    if (this.stopFlag) return;

    this.bars.forEach((b, idx) => {
      if (b.state !== 'sorted') {
        b.state = (idx === this.currentPivotIndex) ? 'pivot' : 'default';
      }
    });

    switch (step.type) {
      case 'compare':
        this.bars[step.indices[0]].state = 'compare';
        this.bars[step.indices[1]].state = 'compare';
        this.soundForStep('compare', this.bars[step.indices[0]].value);
        this.liveComparisons++;
        break;

      case 'swap': {
        this.bars[step.indices[0]].state = 'swap';
        this.bars[step.indices[1]].state = 'swap';
        const tmp = this.bars[step.indices[0]].value;
        this.bars[step.indices[0]].value = this.bars[step.indices[1]].value;
        this.bars[step.indices[1]].value = tmp;
        if (step.indices[0] === this.currentPivotIndex) this.currentPivotIndex = step.indices[1];
        else if (step.indices[1] === this.currentPivotIndex) this.currentPivotIndex = step.indices[0];
        this.soundForStep('swap', this.bars[step.indices[0]].value);
        this.liveInterchanges++;
        break;
      }

      case 'overwrite':
        this.bars[step.indices[0]].state = 'swap';
        this.bars[step.indices[0]].value = step.values![0];
        this.soundForStep('overwrite', step.values![0]);
        this.liveInterchanges++;
        break;

      case 'markSorted':
        this.bars[step.indices[0]].state = 'sorted';
        if (step.indices[0] === this.currentPivotIndex) this.currentPivotIndex = -1;
        this.soundForStep('markSorted');
        break;

      case 'pivot':
        this.currentPivotIndex = step.indices[0];
        this.bars[step.indices[0]].state = 'pivot';
        this.soundForStep('pivot');
        break;
    }
  }

  private applyStepSilent(step: SortStep) {
    this.bars.forEach((b, idx) => {
      if (b.state !== 'sorted') {
        b.state = (idx === this.currentPivotIndex) ? 'pivot' : 'default';
      }
    });

    switch (step.type) {
      case 'compare':
        this.bars[step.indices[0]].state = 'compare';
        this.bars[step.indices[1]].state = 'compare';
        this.liveComparisons++;
        break;

      case 'swap': {
        this.bars[step.indices[0]].state = 'swap';
        this.bars[step.indices[1]].state = 'swap';
        const tmp = this.bars[step.indices[0]].value;
        this.bars[step.indices[0]].value = this.bars[step.indices[1]].value;
        this.bars[step.indices[1]].value = tmp;
        if (step.indices[0] === this.currentPivotIndex) this.currentPivotIndex = step.indices[1];
        else if (step.indices[1] === this.currentPivotIndex) this.currentPivotIndex = step.indices[0];
        this.liveInterchanges++;
        break;
      }

      case 'overwrite':
        this.bars[step.indices[0]].state = 'swap';
        this.bars[step.indices[0]].value = step.values![0];
        this.liveInterchanges++;
        break;

      case 'markSorted':
        this.bars[step.indices[0]].state = 'sorted';
        if (step.indices[0] === this.currentPivotIndex) this.currentPivotIndex = -1;
        break;

      case 'pivot':
        this.currentPivotIndex = step.indices[0];
        this.bars[step.indices[0]].state = 'pivot';
        break;
    }
  }

  private replayTo(targetStep: number) {
    this.bars = [...this.response.originalArray].map(v => ({ value: v, state: 'default' as BarState }));
    this.liveComparisons  = 0;
    this.liveInterchanges = 0;
    this.currentPivotIndex = -1;
    for (let i = 0; i < targetStep; i++) this.applyStepSilent(this.steps[i]);
  }

  private delay(ms: number): Promise<void> {
    return new Promise(res => setTimeout(res, ms));
  }
}
