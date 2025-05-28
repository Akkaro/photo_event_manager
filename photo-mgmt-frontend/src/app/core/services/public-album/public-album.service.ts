import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
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
    // Important: This is a public endpoint, so we don't need authentication headers
    const url = `${environment.apiUrl}/v1/public/album/${token}`;

    console.log('PublicAlbumService: Making request to:', url);

    // Create headers without credentials for public access
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    });

    return this.http.get<PublicAlbumResponse>(url, {
      headers,
      // Don't send credentials for public endpoints
      withCredentials: false
    });
  }
}
