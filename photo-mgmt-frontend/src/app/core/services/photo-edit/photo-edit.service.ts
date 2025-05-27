import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ROUTES } from '../../config/routes.enum';

export interface PhotoEditRequest {
  photoId: string;
  brightness?: number;
  contrast?: number;
}

export interface PhotoEditResponse {
  editId: string;
  photoId: string;
  ownerId: string;
  ownerName: string;
  brightness: number;
  contrast: number;
  editedAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class PhotoEditService {

  constructor(private http: HttpClient) { }

  editPhoto(photoId: string, editRequest: PhotoEditRequest): Observable<PhotoEditResponse> {
    return this.http.post<PhotoEditResponse>(`/v1/${ROUTES.PHOTOS}/${photoId}/edit`, editRequest);
  }
}
