import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface PublicPhoto {
  photoName: string;
  path: string;
  uploadedAt: string;
  isEdited: boolean;
}

export interface PublicAlbumResponse {
  albumName: string;
  ownerName: string;
  createdAt: string;
  photoCount: number;
  photos: PublicPhoto[];
}

@Injectable({
  providedIn: 'root'
})
export class PublicAlbumService {

  constructor(private http: HttpClient) { }

  /**
   * Get public album data by token - this calls the BACKEND API
   */
  getPublicAlbum(token: string): Observable<PublicAlbumResponse> {
    // This should call your backend API, not the frontend
    return this.http.get<PublicAlbumResponse>(`${environment.apiUrl}/v1/public/album/${token}`);
  }
}
