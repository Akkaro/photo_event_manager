import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ROUTES } from '../../../core/config/routes.enum';
import {PhotoEditRequest} from '../../../feature/photo-edits/models/photo-edit-request.model';
import {PhotoEditResponse} from '../../../feature/photo-edits/models/photo-edit-response.model';


@Injectable({
  providedIn: 'root'
})
export class PhotoEditService {

  constructor(private http: HttpClient) { }

  editPhoto(photoId: string, editRequest: PhotoEditRequest): Observable<PhotoEditResponse> {
    return this.http.post<PhotoEditResponse>(`/v1/${ROUTES.PHOTOS}/${photoId}/edit`, editRequest);
  }

  editBrightnessContrast(photoId: string, brightness: number = 0, contrast: number = 1.0): Observable<PhotoEditResponse> {
    return this.http.post<PhotoEditResponse>(
      `/v1/${ROUTES.PHOTOS}/${photoId}/edit/brightness-contrast`,
      null,
      { params: { brightness: brightness.toString(), contrast: contrast.toString() } }
    );
  }

  editGamma(photoId: string, gamma: number): Observable<PhotoEditResponse> {
    return this.http.post<PhotoEditResponse>(
      `/v1/${ROUTES.PHOTOS}/${photoId}/edit/gamma`,
      null,
      { params: { gamma: gamma.toString() } }
    );
  }

  editHistogramEqualization(photoId: string): Observable<PhotoEditResponse> {
    return this.http.post<PhotoEditResponse>(`/v1/${ROUTES.PHOTOS}/${photoId}/edit/histogram-equalization`, null);
  }

  editBlur(photoId: string, kernelSize: number, sigma: number = 2.0): Observable<PhotoEditResponse> {
    return this.http.post<PhotoEditResponse>(
      `/v1/${ROUTES.PHOTOS}/${photoId}/edit/blur`,
      null,
      { params: { kernelSize: kernelSize.toString(), sigma: sigma.toString() } }
    );
  }

  editEdgeDetection(photoId: string, type: 'canny' | 'sobel'): Observable<PhotoEditResponse> {
    return this.http.post<PhotoEditResponse>(
      `/v1/${ROUTES.PHOTOS}/${photoId}/edit/edge-detection`,
      null,
      { params: { type } }
    );
  }

  editMorphological(photoId: string, operation: 'open' | 'close', kernelSize: number, iterations: number = 1): Observable<PhotoEditResponse> {
    return this.http.post<PhotoEditResponse>(
      `/v1/${ROUTES.PHOTOS}/${photoId}/edit/morphological`,
      null,
      { params: { operation, kernelSize: kernelSize.toString(), iterations: iterations.toString() } }
    );
  }

  editDenoise(photoId: string, type: 'bilateral' | 'median'): Observable<PhotoEditResponse> {
    return this.http.post<PhotoEditResponse>(
      `/v1/${ROUTES.PHOTOS}/${photoId}/edit/denoise`,
      null,
      { params: { type } }
    );
  }

  editThreshold(photoId: string, threshold: number, type: string = 'binary'): Observable<PhotoEditResponse> {
    return this.http.post<PhotoEditResponse>(
      `/v1/${ROUTES.PHOTOS}/${photoId}/edit/threshold`,
      null,
      { params: { threshold: threshold.toString(), type } }
    );
  }

  editAutoThreshold(photoId: string): Observable<PhotoEditResponse> {
    return this.http.post<PhotoEditResponse>(`/v1/${ROUTES.PHOTOS}/${photoId}/edit/auto-threshold`, null);
  }

  editHsvConvert(photoId: string): Observable<PhotoEditResponse> {
    return this.http.post<PhotoEditResponse>(`/v1/${ROUTES.PHOTOS}/${photoId}/edit/hsv-convert`, null);
  }
}
