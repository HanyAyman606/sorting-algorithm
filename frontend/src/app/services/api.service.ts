import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { VisualizationRequest, VisualizationResponse } from '../models/visualization.model';
import { BenchmarkRequest, BenchmarkResponse } from '../models/benchmark.model';

@Injectable({ providedIn: 'root' })
export class ApiService {

  private readonly BASE = 'http://localhost:8080/api/sort';

  constructor(private http: HttpClient) {}

  visualize(request: VisualizationRequest): Observable<VisualizationResponse> {
    return this.http.post<VisualizationResponse>(`${this.BASE}/visualize`, request);
  }

  benchmark(request: BenchmarkRequest): Observable<BenchmarkResponse[]> {
    return this.http.post<BenchmarkResponse[]>(`${this.BASE}/benchmark`, request);
  }
}